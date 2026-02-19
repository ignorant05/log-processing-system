package com.github.ignorant05.log_processing_system.util;

/** MetricsSnapShot */
public final class MetricsSnapShot {

  private final long producedMessages;
  private final long consumedMessages;
  private final double producedPerSecond;
  private final double consumedPerSecond;
  private final long errorCount;
  private final long retryCount;
  private final double averageLatencyMS;
  private final long maxLatencyMS;
  private final long uptimeSeconds;

  public MetricsSnapShot(
      long producedMessages,
      long consumedMessages,
      double producedPerSecond,
      double consumedPerSecond,
      long errorCount,
      long retryCount,
      double averageLatencyMS,
      long maxLatencyMS,
      long uptimeSeconds) {
    this.producedMessages = producedMessages;
    this.consumedMessages = consumedMessages;
    this.producedPerSecond = producedPerSecond;
    this.consumedPerSecond = consumedPerSecond;
    this.errorCount = errorCount;
    this.retryCount = retryCount;
    this.averageLatencyMS = averageLatencyMS;
    this.maxLatencyMS = maxLatencyMS;
    this.uptimeSeconds = uptimeSeconds;
  }

  public long getProducedMessages() {
    return this.producedMessages;
  }

  public long getConsumedMessages() {
    return this.consumedMessages;
  }

  public double getProducedPerSecond() {
    return this.producedPerSecond;
  }

  public double getConsumedPerSecond() {
    return this.consumedPerSecond;
  }

  public long getErrorCount() {
    return this.errorCount;
  }

  public long getRetryCount() {
    return this.retryCount;
  }

  public double getAverageLatencyMS() {
    return this.averageLatencyMS;
  }

  public long getMaxLatencyMS() {
    return this.maxLatencyMS;
  }

  public long getUptimeSeconds() {
    return this.uptimeSeconds;
  }
}
