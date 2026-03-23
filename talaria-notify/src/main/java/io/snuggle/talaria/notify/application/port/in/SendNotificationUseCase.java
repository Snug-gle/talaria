package io.snuggle.talaria.notify.application.port.in;

import io.snuggle.talaria.common.event.StockAnalyzedEvent;
import io.snuggle.talaria.notify.domain.vo.ChannelType;

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
