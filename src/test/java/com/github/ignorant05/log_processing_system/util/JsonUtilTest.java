package com.github.ignorant05.log_processing_system.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ignorant05.log_processing_system.model.LogEntry;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

/** JsonUtilTest */
public class JsonUtilTest {

  @Test
  void shouldSerializeToJson() throws JsonProcessingException {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    String id = "log-123",
        level = "WARN",
        service = "transaction-service",
        message = "Payment Succeeded",
        userID = "user-123",
        ipAddr = "192.168.0.0";
    int durationMS = 3600;

    LogEntry logEntry =
        LogEntry.builder()
            .id(id)
            .timestamp(now)
            .level(level)
            .service(service)
            .message(message)
            .userID(userID)
            .ipAddr(ipAddr)
            .durationMS(durationMS)
            .build();

    String jsonString = JsonUtil.toJson(logEntry);
    assertNotNull(jsonString);

    LogEntry deserialized = JsonUtil.fromJson(jsonString);
    assertNotNull(deserialized);

    assertEquals(logEntry.getID(), deserialized.getID());
    assertEquals(logEntry.getTimestamp(), deserialized.getTimestamp());
    assertEquals(logEntry.getLevel(), deserialized.getLevel());
    assertEquals(logEntry.getService(), deserialized.getService());
    assertEquals(logEntry.getMessage(), deserialized.getMessage());
    assertEquals(logEntry.getUserID(), deserialized.getUserID());
    assertEquals(logEntry.getIpAddr(), deserialized.getIpAddr());
    assertEquals(logEntry.getDurationMS(), deserialized.getDurationMS());
  }

  @Test
  void shouldDeserializeFromJson() throws JsonProcessingException {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    String id = "log-123",
        level = "WARN",
        service = "transaction-service",
        message = "Payment Succeeded",
        userID = "user-123",
        ipAddr = "192.168.0.0";
    int durationMS = 3600;

    String json =
        String.format(
            "{"
                + "\"id\":\"%s\","
                + "\"timestamp\":\"%s\","
                + "\"level\":\"%s\","
                + "\"service\":\"%s\","
                + "\"message\":\"%s\","
                + "\"userID\":\"%s\","
                + "\"ipAddr\":\"%s\","
                + "\"durationMS\":%d"
                + "}",
            id, now.toString(), level, service, message, userID, ipAddr, durationMS);

    LogEntry logEntry = JsonUtil.fromJson(json);

    assertEquals(id, logEntry.getID());
    assertEquals(now, logEntry.getTimestamp());
    assertEquals(level, logEntry.getLevel());
    assertEquals(service, logEntry.getService());
    assertEquals(message, logEntry.getMessage());
    assertEquals(userID, logEntry.getUserID());
    assertEquals(ipAddr, logEntry.getIpAddr());
    assertEquals(durationMS, logEntry.getDurationMS());
  }

  @Test
  void shouldProducePrettyJson() throws JsonProcessingException {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    String id = "log-123",
        level = "WARN",
        service = "transaction-service",
        message = "Payment Succeeded",
        userID = "user-123",
        ipAddr = "192.168.0.0";
    int durationMS = 3600;

    LogEntry logEntry =
        LogEntry.builder()
            .id(id)
            .timestamp(now)
            .level(level)
            .service(service)
            .message(message)
            .userID(userID)
            .ipAddr(ipAddr)
            .durationMS(durationMS)
            .build();

    String prettyJsonString = JsonUtil.toPrettyJson(logEntry);
    assertNotNull(prettyJsonString);

    assertTrue(prettyJsonString.contains("\n"));
    assertTrue(prettyJsonString.contains("  "));
  }

  @Test
  void shouldValidateJson() {

    String validJson = "{\"id\":\"test\",\"level\":\"INFO\"}";
    String invalidJson = "{obviously not a json}";
    String emptyJson = "";

    assertTrue(JsonUtil.isValid(validJson));
    assertFalse(JsonUtil.isValid(invalidJson));
    assertTrue(JsonUtil.isValid(emptyJson));
  }

  @Test
  void shouldHandleMissingFieldsInJson() throws JsonProcessingException {
    String id = "log-123",
        level = "WARN",
        service = "transaction-service",
        message = "Payment Succeeded",
        ipAddr = "192.168.0.0";

    String json =
        String.format(
            "{"
                + "\"id\":\"%s\","
                + "\"level\":\"%s\","
                + "\"service\":\"%s\","
                + "\"message\":\"%s\","
                + "\"ipAddr\":\"%s\""
                + "}",
            id, level, service, message, ipAddr);

    LogEntry deserialized = JsonUtil.fromJson(json);

    assertNotNull(deserialized);

    assertEquals(id, deserialized.getID());
    assertEquals(level, deserialized.getLevel());
    assertEquals(service, deserialized.getService());
    assertEquals(message, deserialized.getMessage());
    assertNull(deserialized.getUserID());
    assertEquals(ipAddr, deserialized.getIpAddr());
    assertEquals(0, deserialized.getDurationMS());
  }
}
