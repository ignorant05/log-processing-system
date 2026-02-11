package com.github.ignorant05.log_processing_system;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ignorant05.log_processing_system.model.LogEntry;
import com.github.ignorant05.log_processing_system.util.JsonUtil;
import java.time.Instant;

public class LogProcessingSystemApplication {
  public static void main(String[] args) {

    System.out.println("=== Kafka Log Processing System ===\n");

    LogEntry logEntry =
        LogEntry.builder()
            .id("log-" + System.currentTimeMillis())
            .timestamp(Instant.now())
            .level("INFO")
            .service("main-app")
            .message("System started successfully")
            .userID("system")
            .ipAddr("127.0.0.1")
            .durationMS(0)
            .build();

    System.out.println("Created LogEntry:");
    System.out.println(logEntry);

    try {
      String json = JsonUtil.toJson(logEntry);
      System.out.println("\nAs JSON:");
      System.out.println(json);

      String prettyJson = JsonUtil.toPrettyJson(logEntry);
      System.out.println("\nAs Pretty JSON:");
      System.out.println(prettyJson);

      LogEntry deserialized = JsonUtil.fromJson(json);
      System.out.println("\nDeserialized LogEntry:");
      System.out.println(deserialized);

      System.out.println("\nAre they equal? " + logEntry.equals(deserialized));

    } catch (JsonProcessingException e) {
      System.err.println("Error processing JSON: " + e.getMessage());
    }
  }
}
