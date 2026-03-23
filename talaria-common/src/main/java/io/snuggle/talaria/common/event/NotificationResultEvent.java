package io.snuggle.talaria.common.event;

import java.time.LocalDateTime;

public record NotificationResultEvent(
        String eventId,
        String notificationId,
        String channel,
        String status,
        LocalDateTime processedAt,
        String errorMessage
) {}
