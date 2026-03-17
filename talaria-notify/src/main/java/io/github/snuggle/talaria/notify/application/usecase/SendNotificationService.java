package io.github.snuggle.talaria.notify.application.usecase;

import io.github.snuggle.talaria.common.event.NotificationResultEvent;
import io.github.snuggle.talaria.common.event.StockAnalyzedEvent;
import io.github.snuggle.talaria.notify.adapter.out.channel.ChannelAdapter;
import io.github.snuggle.talaria.notify.application.port.in.SendNotificationUseCase;
import io.github.snuggle.talaria.notify.application.port.out.PublishNotificationEventPort;
import io.github.snuggle.talaria.notify.application.port.out.SaveNotificationPort;
import io.github.snuggle.talaria.notify.domain.model.Notification;
import io.github.snuggle.talaria.notify.domain.model.NotificationDispatch;
import io.github.snuggle.talaria.notify.domain.vo.ChannelType;
import io.github.snuggle.talaria.notify.domain.vo.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SendNotificationService implements SendNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendNotificationService.class);

    private final SaveNotificationPort saveNotificationPort;
    private final PublishNotificationEventPort publishNotificationEventPort;
    private final Map<ChannelType, ChannelAdapter> channelAdapters;

    public SendNotificationService(
        SaveNotificationPort saveNotificationPort,
        PublishNotificationEventPort publishNotificationEventPort,
        List<ChannelAdapter> channelAdapters
    ) {
        this.saveNotificationPort = saveNotificationPort;
        this.publishNotificationEventPort = publishNotificationEventPort;
        this.channelAdapters = channelAdapters.stream()
            .collect(Collectors.toMap(ChannelAdapter::channel, Function.identity()));
    }

    @Override
    public void sendFromEvent(StockAnalyzedEvent event) {
        String content = buildContent(event);
        Notification notification = new Notification(
            "오늘의 배당주 분석 결과",
            content,
            List.of(ChannelType.values()),
            Priority.NORMAL,
            "STOCK_ANALYZED",
            event.eventId()
        );
        send(notification);
    }

    @Override
    public void sendManual(SendNotificationCommand command) {
        Notification notification = new Notification(
            command.title(),
            command.content(),
            command.channels(),
            Priority.HIGH,
            "MANUAL",
            UUID.randomUUID().toString()
        );
        send(notification);
    }

    private void send(Notification notification) {
        Notification saved = saveNotificationPort.saveNotification(notification);

        // Virtual Threads로 채널별 병렬 발송 (개별 채널 오류는 dispatchToChannel 내부에서 처리)
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            saved.getChannels().forEach(channel ->
                executor.submit(() -> dispatchToChannel(saved, channel))
            );
        }
    }

    private void dispatchToChannel(Notification notification, ChannelType channelType) {
        ChannelAdapter adapter = channelAdapters.get(channelType);
        NotificationDispatch dispatch = new NotificationDispatch(notification.getId(), channelType);

        if (adapter == null || !adapter.isAvailable()) {
            log.warn("채널 사용 불가 | channel={}", channelType);
            dispatch.markFailed("채널 어댑터 없음 또는 비활성화");
            saveNotificationPort.saveDispatch(dispatch);
            return;
        }

        dispatch.markSending();
        saveNotificationPort.saveDispatch(dispatch);

        try {
            ChannelAdapter.SendResult result = adapter.send(notification);
            if (result.success()) {
                dispatch.markSent(result.messageId());
                publishNotificationEventPort.publishResult(toResultEvent(dispatch, notification.getId().toString(), null));
            } else {
                dispatch.markFailed(result.errorMessage());
                publishNotificationEventPort.publishDlq(toResultEvent(dispatch, notification.getId().toString(), result.errorMessage()));
            }
        } catch (Exception e) {
            log.error("채널 발송 오류 | channel={} | error={}", channelType, e.getMessage());
            dispatch.markFailed(e.getMessage());
            publishNotificationEventPort.publishDlq(toResultEvent(dispatch, notification.getId().toString(), e.getMessage()));
        } finally {
            saveNotificationPort.updateDispatch(dispatch);
        }
    }

    private NotificationResultEvent toResultEvent(NotificationDispatch dispatch, String notificationId, String errorMessage) {
        return new NotificationResultEvent(
            UUID.randomUUID().toString(),
            notificationId,
            dispatch.getChannel().name(),
            dispatch.getStatus().name(),
            LocalDateTime.now(),
            errorMessage
        );
    }

    private String buildContent(StockAnalyzedEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Talaria] ").append(event.analysisDate()).append(" 배당주 분석 결과\n\n");
        event.stocks().forEach(stock ->
            sb.append(String.format("• %s(%s) | %s | 예상수익률 %.2f%%\n  %s\n",
                stock.name(), stock.ticker(), stock.grade(),
                stock.expectedYield(), stock.summary()))
        );
        return sb.toString();
    }
}
