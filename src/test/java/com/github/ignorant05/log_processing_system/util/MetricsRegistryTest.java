package com.github.ignorant05.log_processing_system.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** MetricsRegistryTest */
public class MetricsRegistryTest {
  private MetricsRegistry registry;

  @BeforeEach
  void setup() {
    registry = MetricsRegistry.getInstance();
    registry.reset();
  }

  @Test
  void initialSnapshot_allCountersAreNull() {
    MetricsSnapShot snapshot = registry.snapshot();

    assertEquals(0, snapshot.getProducedMessages());
    assertEquals(0, snapshot.getConsumedMessages());
    assertEquals(0, snapshot.getErrorCount());
    assertEquals(0, snapshot.getRetryCount());
    assertEquals(0.0, snapshot.getAverageLatencyMS());
    assertEquals(0, snapshot.getMaxLatencyMS());
  }

  @Test
  void recordProduced_incrementCount() {
    registry.recordProduced();
    registry.recordProduced();
    registry.recordProduced(4);

    assertEquals(6, registry.snapshot().getProducedMessages());
  }

  @Test
  void recordConsumed_incrementCount() {
    registry.recordConsumed();
    registry.recordConsumed(17);

    assertEquals(18, registry.snapshot().getConsumedMessages());
  }

  @Test
  void errorCount_And_retryCount_incrementSeparately() {
    registry.recordError();
    registry.recordError();
    registry.recordRetry();
    registry.recordError();

    MetricsSnapShot snapshot = registry.snapshot();

    assertEquals(3, snapshot.getErrorCount());
    assertEquals(1, snapshot.getRetryCount());
  }

  @Test
  void recordLatency_conputesAvgAndMax() {
    registry.recordLatency(2);
    registry.recordLatency(4);
    registry.recordLatency(9);

    MetricsSnapShot snapshot = registry.snapshot();

    assertEquals(5.0, snapshot.getAverageLatencyMS());
    assertEquals(9, snapshot.getMaxLatencyMS());
  }

  @Test
  void recordLatency_maxTracksHighestValue() {
    registry.recordLatency(100);
    registry.recordLatency(50);
    registry.recordLatency(200);
    registry.recordLatency(75);

    assertEquals(200, registry.snapshot().getMaxLatencyMS());
  }

  @Test
  void producedPerSecond_isPositive_afterRecording() {
    registry.recordProduced(100);

    assertTrue(registry.snapshot().getProducedPerSecond() > 0);
  }

  @Test
  void reset_clearsAllCounters() {
    registry.recordProduced(10);
    registry.recordConsumed(5);
    registry.recordError();
    registry.recordLatency(99);

    registry.reset();
    MetricsSnapShot s = registry.snapshot();

    assertEquals(0, s.getProducedMessages());
    assertEquals(0, s.getConsumedMessages());
    assertEquals(0, s.getErrorCount());
    assertEquals(0.0, s.getAverageLatencyMS());
  }
}
