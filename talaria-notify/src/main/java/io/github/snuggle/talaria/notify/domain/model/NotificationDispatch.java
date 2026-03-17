package io.github.snuggle.talaria.notify.domain.model;

import io.github.snuggle.talaria.notify.domain.vo.ChannelType;
import io.github.snuggle.talaria.notify.domain.vo.DispatchStatus;

import java.time.LocalDateTime;

public class NotificationDispatch {

    private Long id;
    private Long notificationId;
    private ChannelType channel;
    private DispatchStatus status;
    private String externalMessageId;
    private int retryCount;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;

    public NotificationDispatch(Long notificationId, ChannelType channel) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = DispatchStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void markSending() {
        this.status = DispatchStatus.SENDING;
    }

    public void markSent(String externalMessageId) {
        this.status = DispatchStatus.SENT;
        this.externalMessageId = externalMessageId;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = DispatchStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markRetry(String errorMessage) {
        this.status = DispatchStatus.RETRY;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    public void markDlq(String errorMessage) {
        this.status = DispatchStatus.DLQ;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getNotificationId() { return notificationId; }
    public ChannelType getChannel() { return channel; }
    public DispatchStatus getStatus() { return status; }
    public String getExternalMessageId() { return externalMessageId; }
    public int getRetryCount() { return retryCount; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
