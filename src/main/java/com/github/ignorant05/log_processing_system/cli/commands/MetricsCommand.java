package com.github.ignorant05.log_processing_system.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ignorant05.log_processing_system.util.MetricsRegistry;
import com.github.ignorant05.log_processing_system.util.MetricsSnapShot;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** MetricsCommand */
@Command(
    name = "metrics",
    description = "Display system metrics: throughput, latency, errors, and retries.",
    mixinStandardHelpOptions = true)
public class MetricsCommand implements Callable<Integer> {

  @Option(
      names = {"--json"},
      description = "Output metrics as JSON")
  private boolean useJSON;

  @Option(
      names = {"--watch", "-w"},
      description = "Continuously refresh metrics every N seconds",
      defaultValue = "0")
  private int watchSeconds;

  public Integer call() throws InterruptedException {
    if (watchSeconds > 0) {
      runWatchMode();
    } else {
      printSnapshot(MetricsRegistry.getInstance().snapshot());
    }
    return 0;
  }

  private void runWatchMode() throws InterruptedException {
    System.out.printf("Refreshing every %ds — press Ctrl-C to stop%n%n", watchSeconds);
    while (!Thread.currentThread().isInterrupted()) {
      printSnapshot(MetricsRegistry.getInstance().snapshot());
      Thread.sleep(watchSeconds * 1000L);
      System.out.print("\033[13A");
    }
  }

  private void printSnapshot(MetricsSnapShot snapshot) {
    if (useJSON) {
      printJson(snapshot);
    } else {
      printHuman(snapshot);
    }
  }

  private void printHuman(MetricsSnapShot snapshot) {
    String uptime = formatUptime(snapshot.getUptimeSeconds());
    System.out.println("┌──────────────────────────────────────────────────────┐");
    System.out.println("│                   SYSTEM METRICS                    │");
    System.out.printf("│  Uptime: %-43s│%n", uptime);
    System.out.println("├──────────────────────────────────────────────────────┤");
    System.out.println("│  THROUGHPUT                                          │");
    System.out.printf(
        "│    Produced       %10d msg   (%6.1f msg/snapshot)    │%n",
        snapshot.getProducedMessages(), snapshot.getProducedPerSecond());
    System.out.printf(
        "│    Consumed       %10d msg   (%6.1f msg/snapshot)    │%n",
        snapshot.getConsumedMessages(), snapshot.getConsumedPerSecond());
    System.out.println("├──────────────────────────────────────────────────────┤");
    System.out.println("│  LATENCY                                             │");
    System.out.printf(
        "│    Avg            %10.1f ms                      │%n", snapshot.getAverageLatencyMS());
    System.out.printf(
        "│    Max            %10d ms                      │%n", snapshot.getMaxLatencyMS());
    System.out.println("├──────────────────────────────────────────────────────┤");
    System.out.println("│  RELIABILITY                                         │");
    System.out.printf(
        "│    Errors         %10d                          │%n", snapshot.getErrorCount());
    System.out.printf(
        "│    Retries        %10d                          │%n", snapshot.getRetryCount());
    System.out.println("└──────────────────────────────────────────────────────┘");
  }

  private void printJson(MetricsSnapShot snapshot) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode root = mapper.createObjectNode();

      ObjectNode throughput = mapper.createObjectNode();
      throughput.put("messages_produced", snapshot.getProducedMessages());
      throughput.put("messages_consumed", snapshot.getConsumedMessages());
      throughput.put("produced_per_second", snapshot.getProducedPerSecond());
      throughput.put("consumed_per_second", snapshot.getConsumedPerSecond());

      ObjectNode latency = mapper.createObjectNode();
      latency.put("avg_ms", snapshot.getAverageLatencyMS());
      latency.put("max_ms", snapshot.getMaxLatencyMS());

      ObjectNode reliability = mapper.createObjectNode();
      reliability.put("error_count", snapshot.getErrorCount());
      reliability.put("retry_count", snapshot.getRetryCount());

      root.put("uptime_seconds", snapshot.getUptimeSeconds());
      root.set("throughput", throughput);
      root.set("latency", latency);
      root.set("reliability", reliability);

      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    } catch (Exception e) {
      System.err.println("Failed to serialize JSON: " + e.getMessage());
    }
  }

  private static String formatUptime(long uptimeSeconds) {
    long h = uptimeSeconds / 3600;
    long m = (uptimeSeconds % 3600) / 60;
    long s = uptimeSeconds % 60;

    return String.format("%dh %02dm %02ds", h, m, s);
  }
}
