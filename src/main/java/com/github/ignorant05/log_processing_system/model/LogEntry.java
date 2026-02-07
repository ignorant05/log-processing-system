package com.github.ignorant05.log_processing_system.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * LogEntry
 */
public class LogEntry {
	private String id;
	private Instant timestamp;
	private String level;
	private String service;
	private String message;
	private String userID;
	private String ipAddr;
	private int durationMS;

	public LogEntry() {
	}

	public LogEntry(
			String id,
			Instant timestamp,
			String level,
			String service,
			String message,
			String userID,
			String ipAddr,
			int durationMS) {
		this.id = id;
		this.timestamp = timestamp.truncatedTo(ChronoUnit.MILLIS);
		this.level = level;
		this.service = service;
		this.message = message;
		this.userID = userID;
		this.ipAddr = ipAddr;
		this.durationMS = durationMS;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getID() {
		return this.id;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp.truncatedTo(ChronoUnit.MILLIS);
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
	public Instant getTimestamp() {
		return this.timestamp.truncatedTo(ChronoUnit.MILLIS);
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getLevel() {
		return this.level;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getService() {
		return this.service;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserID() {
		return this.userID;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public String getIpAddr() {
		return this.ipAddr;
	}

	public void setDurationMS(int durationMS) {
		this.durationMS = durationMS;
	}

	public int getDurationMS() {
		return this.durationMS;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String id;
		private Instant timestamp;
		private String level;
		private String service;
		private String message;
		private String userID;
		private String ipAddr;
		private int durationMS;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder timestamp(Instant timestamp) {
			this.timestamp = timestamp.truncatedTo(ChronoUnit.MILLIS);
			return this;
		}

		public Builder level(String level) {
			this.level = level;
			return this;
		}

		public Builder service(String service) {
			this.service = service;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder userID(String userID) {
			this.userID = userID;
			return this;
		}

		public Builder ipAddr(String ipAddr) {
			this.ipAddr = ipAddr;
			return this;
		}

		public Builder durationMS(int durationMS) {
			this.durationMS = durationMS;
			return this;
		}

		public LogEntry build() {
			return new LogEntry(id, timestamp, level, service, message, userID, ipAddr, durationMS);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		LogEntry logEntry = (LogEntry) obj;

		return Objects.equals(id, logEntry.id) &&
				Objects.equals(timestamp, logEntry.timestamp) &&
				Objects.equals(level, logEntry.level) &&
				Objects.equals(service, logEntry.service) &&
				Objects.equals(message, logEntry.message) &&
				Objects.equals(userID, logEntry.userID) &&
				Objects.equals(ipAddr, logEntry.ipAddr) &&
				durationMS == logEntry.durationMS;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, timestamp, level, service, message, userID, ipAddr, durationMS);
	}

	@Override
	public String toString() {
		return "LogEntry{" +
				"id='" + id + '\'' +
				", timestamp=" + timestamp +
				", level='" + level + '\'' +
				", service='" + service + '\'' +
				", message='" + message + '\'' +
				", userID='" + userID + '\'' +
				", ipAddr='" + ipAddr + '\'' +
				", durationMs=" + durationMS +
				'}';
	}
}
