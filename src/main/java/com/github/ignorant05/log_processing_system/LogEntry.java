package com.github.ignorant05.log_processing_system;

import java.time.Instant;
import java.util.UUID;

public class LogEntry {
    private String id;
    private String level;
    private String message;
    private String timestamp;

    public LogEntry(String level, String message) {
        this.id = UUID.randomUUID().toString();
        this.level = level;
        this.message = message;
        this.timestamp = Instant.now().toString();
    }

    // Getters
    public String getId() { return id; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }

    // Serialization for the task
    public String toJson() {
        return String.format(
                "{\"id\":\"%s\", \"level\":\"%s\", \"message\":\"%s\", \"timestamp\":\"%s\"}",
                id, level, message, timestamp
        );
    }
}
