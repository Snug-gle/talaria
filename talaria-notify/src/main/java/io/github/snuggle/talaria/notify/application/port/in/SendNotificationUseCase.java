package io.github.snuggle.talaria.notify.application.port.in;

import io.github.snuggle.talaria.common.event.StockAnalyzedEvent;
import io.github.snuggle.talaria.notify.domain.vo.ChannelType;

import java.util.List;

public interface SendNotificationUseCase {

    void sendFromEvent(StockAnalyzedEvent event);

    void sendManual(SendNotificationCommand command);

    record SendNotificationCommand(
        String title,
        String content,
        List<ChannelType> channels
    ) {}
}
