package com.example.realtime.common.dto;

import java.time.Instant;

/**
 * Lightweight DTO representing an incoming event.
 * This class is part of the shared `common` module used by all services.
 */
public class EventDTO {
    private String eventType;
    private String userId;
    private Instant timestamp;
    private Object metadata;

    public EventDTO() {}

    public EventDTO(String eventType, String userId, Instant timestamp, Object metadata) {
        this.eventType = eventType;
        this.userId = userId;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    // Getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Object getMetadata() { return metadata; }
    public void setMetadata(Object metadata) { this.metadata = metadata; }
}
