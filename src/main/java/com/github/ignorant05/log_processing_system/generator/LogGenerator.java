package com.github.ignorant05.log_processing_system.generator;

import com.github.ignorant05.log_processing_system.model.LogEntry;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/** LogGenerator */
public class LogGenerator {
  private static final Random random = new Random();
  private static final String[] LEVELS = {
    "INFO", "WARN", "ERROR", "DEBUG",
  };
  private static final String[] SERVICES = {
    "payment-service",
    "fraud-service",
    "mail-service",
    "api-gateway",
    "database",
    "auth-service",
    "notification-service",
    "cache-service",
  };
  private static final String[] MESSAGES = {
    "Payment success",
    "User logged in successfully",
    "Unauthorized request",
    "Connection pool exhausted",
    "Email sent successfully",
    "User notified",
    "Database query proceeded",
    "High memory usage detected",
  };

  public static LogEntry generateRandomLog() {
    String level = LEVELS[random.nextInt(LEVELS.length)];
    String service = SERVICES[random.nextInt(LEVELS.length)];
    String message = MESSAGES[random.nextInt(LEVELS.length)];

    return LogEntry.builder()
        .id(UUID.randomUUID().toString())
        .timestamp(Instant.now())
        .level(level)
        .service(service)
        .message(message)
        .userID("user-" + random.nextInt(100))
        .ipAddr(generateRandomIP())
        .durationMS(random.nextInt(3600))
        .build();
  }

  public static String generateRandomIP() {
    return "192.168." + random.nextInt(255) + "." + random.nextInt(255);
  }
}
