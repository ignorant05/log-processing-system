package com.github.ignorant05.log_processing_system;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LogEntryTest {

    @Test
    void testLogEntryCreation() {
        LogEntry log = new LogEntry("INFO", "Test log message");
        assertNotNull(log.getId());
        assertEquals("INFO", log.getLevel());
    }

    @Test
    void testJsonFormat() {
        LogEntry log = new LogEntry("DEBUG", "JSON check");
        String json = log.toJson();
        assertTrue(json.contains("\"level\":\"DEBUG\""));
    }
}
