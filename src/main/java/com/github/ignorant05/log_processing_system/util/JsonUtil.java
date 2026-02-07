package com.github.ignorant05.log_processing_system.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.ignorant05.log_processing_system.model.LogEntry;

/**
 * JsonUtil
 */
public class JsonUtil {
	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, true);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	private JsonUtil() {
	}

	public static String toJson(LogEntry logEntry) throws JsonProcessingException {
		return objectMapper.writeValueAsString(logEntry);
	}

	public static LogEntry fromJson(String json) throws JsonProcessingException {
		return objectMapper.readValue(json, LogEntry.class);
	}

	public static String toPrettyJson(LogEntry logEntry) throws JsonProcessingException {
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logEntry);
	}

	public static boolean isValid(String json) {
		try {
			objectMapper.readTree(json);
			return true;
		} catch (JsonProcessingException e) {
			return false;
		}
	}
}
