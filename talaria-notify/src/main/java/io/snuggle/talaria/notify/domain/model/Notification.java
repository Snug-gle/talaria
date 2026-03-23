package io.snuggle.talaria.notify.domain.model;

import io.snuggle.talaria.notify.domain.vo.ChannelType;
import io.snuggle.talaria.notify.domain.vo.Priority;

import java.time.LocalDateTime;
import java.util.List;

public class Notification {

    private Long id;
    private String title;
    private String content;
    private List<ChannelType> channels;
    private Priority priority;
    private String sourceType;  // STOCK_ANALYZED, MANUAL
    private String sourceId;
    private LocalDateTime createdAt;

    public Notification(String title, String content, List<ChannelType> channels,
                        Priority priority, String sourceType, String sourceId) {
        this.title = title;
        this.content = content;
        this.channels = channels;
        this.priority = priority;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public List<ChannelType> getChannels() { return channels; }
    public Priority getPriority() { return priority; }
    public String getSourceType() { return sourceType; }
    public String getSourceId() { return sourceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
