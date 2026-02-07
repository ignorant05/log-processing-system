package com.github.ignorant05.log_processing_system.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/**
 * LogEntryTest
 */
public class LogEntryTest {

	@Test
	void shouldCreateLogEntryWithBuilder() {
		Instant now = Instant.now();
		String id = "log-123", level = "WARN", service = "transaction-service", message = "Payment Succeeded",
				userID = "user-123", ipAddr = "192.168.0.0";
		int durationMS = 3600;

		LogEntry logEntry = LogEntry.builder()
				.id(id)
				.timestamp(now)
				.level(level)
				.service(service)
				.message(message)
				.userID(userID)
				.ipAddr(ipAddr)
				.durationMS(durationMS)
				.build();

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
	void shouldCreateLogEntryWithConstructor() {
		Instant now = Instant.now();
		String id = "log-123", level = "INFO", service = "transaction-service", message = "Payment Succeeded",
				userID = "user-123", ipAddr = "192.168.0.0";
		int durationMS = 3600;

		LogEntry logEntry = new LogEntry(id, now, level, service, message, userID, ipAddr, durationMS);

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
	void shouldSetAndGetFields() {
		Instant now = Instant.now();
		String id = "log-123", level = "INFO", service = "transaction-service", message = "Payment Succeeded",
				userID = "user-123", ipAddr = "192.168.0.0";
		int durationMS = 3600;

		LogEntry logEntry = new LogEntry();

		logEntry.setID(id);
		logEntry.setTimestamp(now);
		logEntry.setLevel(level);
		logEntry.setService(service);
		logEntry.setMessage(message);
		logEntry.setUserID(userID);
		logEntry.setIpAddr(ipAddr);
		logEntry.setDurationMS(durationMS);

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
	void shouldImplementEqualsAndHashCode() {
		Instant now = Instant.now();
		String id_1 = "log-123", level_1 = "INFO", service_1 = "transaction-service", message_1 = "Payment Succeeded",
				userID_1 = "user-123", ipAddr_1 = "192.168.0.0";
		int durationMS_1 = 3600;

		String id_2 = "log-321", level_2 = "ERR", service_2 = "web-app", message_2 = "Authentication Failed",
				userID_2 = "user-321", ipAddr_2 = "192.168.0.1";
		int durationMS_2 = 3800;

		LogEntry logEntry_1 = LogEntry.builder()
				.id(id_1)
				.timestamp(now)
				.level(level_1)
				.service(service_1)
				.message(message_1)
				.userID(userID_1)
				.ipAddr(ipAddr_1)
				.durationMS(durationMS_1)
				.build();

		LogEntry logEntry_2 = LogEntry.builder()
				.id(id_2)
				.timestamp(now)
				.level(level_2)
				.service(service_2)
				.message(message_2)
				.userID(userID_2)
				.ipAddr(ipAddr_2)
				.durationMS(durationMS_2)
				.build();

		LogEntry logEntry_3 = LogEntry.builder()
				.id(id_1)
				.timestamp(now)
				.level(level_1)
				.service(service_1)
				.message(message_1)
				.userID(userID_1)
				.ipAddr(ipAddr_1)
				.durationMS(durationMS_1)
				.build();

		assertEquals(logEntry_1, logEntry_3);
		assertNotEquals(logEntry_1, logEntry_2);
		assertEquals(logEntry_1.hashCode(), logEntry_3.hashCode());
		assertNotEquals(logEntry_1.hashCode(), logEntry_2.hashCode());
	}

	@Test
	void shouldProduceValidStringRepresentation() {
		LogEntry logEntry = LogEntry.builder()
				.id("test-123")
				.timestamp(Instant.parse("2024-01-15T10:30:00Z"))
				.level("DEBUG")
				.service("test-service")
				.message("Test message")
				.build();

		String stringRep = logEntry.toString();

		assertTrue(stringRep.contains("LogEntry{"));
		assertTrue(stringRep.contains("id='test-123'"));
		assertTrue(stringRep.contains("level='DEBUG'"));
		assertTrue(stringRep.contains("service='test-service'"));
	}

}
