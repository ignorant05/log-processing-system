package com.github.ignorant05.log_processing_system.util;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/** MetricsRegistry */
public final class MetricsRegistry {

  private static final MetricsRegistry instance = new MetricsRegistry();

  public static MetricsRegistry getInstance() {
    return instance;
  }

  private MetricsRegistry() {}

  private final AtomicLong producedMessages = new AtomicLong();
  private final AtomicLong consumedMessages = new AtomicLong();
  private final AtomicLong errorCount = new AtomicLong();
  private final AtomicLong retryCount = new AtomicLong();
  private final AtomicLong latencySumMS = new AtomicLong();
  private final AtomicLong latencyMaxMS = new AtomicLong();
  private final AtomicLong latencyCount = new AtomicLong();

  private final Instant startedAt = Instant.now();

  public void recordProduced() {
    this.producedMessages.incrementAndGet();
  }

  public void recordProduced(long count) {
    this.producedMessages.addAndGet(count);
  }

  public void recordConsumed() {
    this.consumedMessages.incrementAndGet();
  }

  public void recordConsumed(long count) {
    this.consumedMessages.addAndGet(count);
  }

  public void recordError() {
    this.errorCount.incrementAndGet();
  }

  public void recordRetry() {
    this.retryCount.incrementAndGet();
  }

  public void recordLatency(long latencyMS) {
    this.latencySumMS.addAndGet(latencyMS);
    this.latencyCount.incrementAndGet();

    long curr;
    do {
      curr = this.latencyMaxMS.get();
    } while (latencyMS > curr && !this.latencyMaxMS.compareAndSet(curr, latencyMS));
  }

  public MetricsSnapShot snapshot() {
    long uptimeSeconds =
        Math.max(1, Instant.now().getEpochSecond() - this.startedAt.getEpochSecond());

    long produced = this.producedMessages.get();
    long consumed = this.consumedMessages.get();
    long count = this.latencyCount.get();

    return new MetricsSnapShot(
        produced,
        consumed,
        (double) produced / uptimeSeconds,
        (double) consumed / uptimeSeconds,
        this.errorCount.get(),
        this.retryCount.get(),
        count == 0 ? 0.0 : (double) this.latencySumMS.get() / count,
        this.latencyMaxMS.get(),
        uptimeSeconds);
  }

  public void reset() {
    this.producedMessages.set(0);
    this.consumedMessages.set(0);
    this.errorCount.set(0);
    this.retryCount.set(0);
    this.latencySumMS.set(0);
    this.latencyMaxMS.set(0);
    this.latencyCount.set(0);
  }
}
