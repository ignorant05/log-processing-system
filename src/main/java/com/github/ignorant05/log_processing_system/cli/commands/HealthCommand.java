package com.github.ignorant05.log_processing_system.cli.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ignorant05.log_processing_system.check.ConsumerCheck;
import com.github.ignorant05.log_processing_system.check.HealthCheckResult;
import com.github.ignorant05.log_processing_system.check.KafkaConnectionCheck;
import com.github.ignorant05.log_processing_system.check.ProducerCheck;
import com.github.ignorant05.log_processing_system.check.TopicAvailabilityCheck;
import com.github.ignorant05.log_processing_system.model.HealthStatus;
import com.github.ignorant05.log_processing_system.service.HealthService;
import com.github.ignorant05.log_processing_system.service.HealthService.HealthReport;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "health",
    description =
        "Checks if:\n"
            + "\t- Kafka broker reachable.\n"
            + "\t- Topic exists.\n"
            + "\t- Producer status.\n"
            + "\t- Consumer status.\n"
            + "\t- brokers available.\n"
            + "\t- partitions available.\n"
            + "\t- replication info.\n"
            + "\t- topic availability check.\n"
            + "\t- producer/consumer status check.",
    mixinStandardHelpOptions = true)
public class HealthCommand implements Callable<Integer> {

  @Option(
      names = {"--bootstrap-servers", "-b"},
      description = "Kafka bootstrap servers",
      defaultValue = "kafka:9092")
  private String bootstrapServers;

  @Option(
      names = {"--topics", "-t"},
      description = "Comma-separated list of required topics",
      defaultValue = "logs",
      split = ",")
  private List<String> topics;

  @Option(
      names = {"--group", "-g"},
      description = "Consumer group ID used for the consumer health check",
      defaultValue = "loggers")
  private String consumerGroup;

  @Option(
      names = {"--timeout"},
      description = "Connection timeout in seconds",
      defaultValue = "5")
  private int timeoutSeconds;

  @Option(
      names = {"--json"},
      description = "Output result in JSON format",
      defaultValue = "false")
  private boolean useJSON;

  @Override
  public Integer call() throws Exception {
    String targetTopic = topics.get(0);
    int timeoutMS = timeoutSeconds * 1000;

    HealthService service =
        new HealthService()
            .register(new KafkaConnectionCheck(bootstrapServers, timeoutSeconds))
            .register(new TopicAvailabilityCheck(bootstrapServers, topics, timeoutSeconds))
            .register(new ProducerCheck(bootstrapServers, targetTopic, timeoutMS))
            .register(new ConsumerCheck(bootstrapServers, consumerGroup, targetTopic, timeoutMS));

    HealthReport report = service.run();

    if (useJSON) printJSON(report);
    else print(report);
    return getExitCode(report.getHealthStatus());
  }

  private void print(HealthReport report) {
    System.out.println("┌─────────────────────────────────────────────────────────┐");
    System.out.println("│                    KAFKA HEALTH CHECK                   │");
    System.out.println("├─────────────────────────────────────────────────────────┤");

    for (HealthCheckResult result : report.getResults()) {
      String icon = getStatusIcon(result.getStatus());
      System.out.printf("| %-6s  %-20s  %s%n", icon, result.getName(), result.getMessage());

      if (result.getDetail() != null) {
        System.out.printf("│         ↳ %s%n", result.getDetail());
      }
    }

    System.out.println("├─────────────────────────────────────────────────────────┤");
    System.out.printf("│ Overall: %-47s│%n", report.getHealthStatus());
    System.out.println("└─────────────────────────────────────────────────────────┘");
  }

  private void printJSON(HealthReport report) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      ObjectNode root = mapper.createObjectNode();
      root.put("status", report.getHealthStatus().name());

      ArrayNode checks = mapper.createArrayNode();
      for (HealthCheckResult result : report.getResults()) {
        ObjectNode node = mapper.createObjectNode();
        node.put("name", result.getName());
        node.put("status", result.getStatus().name());
        node.put("message", result.getMessage());

        if (result.getDetail() != null) {
          node.put("detail", result.getDetail());
        }

        checks.add(node);
      }

      root.set("checks", checks);
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    } catch (Exception e) {
      System.err.println("Failed to serialize JSON output: " + e.getMessage());
    }
  }

  private static String getStatusIcon(HealthStatus status) {
    switch (status) {
      case HEALTHY:
        return "✓ OK  ";
      case DEGRADED:
        return "⚠ WARN";
      case UNHEALTHY:
        return "✗ FAIL";
      default:
        return "?     ";
    }
  }

  private static int getExitCode(HealthStatus status) {
    switch (status) {
      case HEALTHY:
        return 0;
      case DEGRADED:
        return 1;
      case UNHEALTHY:
        return 2;
      default:
        return 3;
    }
  }
}
