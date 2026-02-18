package com.github.ignorant05.log_processing_system.check;

import com.github.ignorant05.log_processing_system.model.HealthCheck;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;

/** TopicAvailabilityCheck */
public class TopicAvailabilityCheck implements HealthCheck {
  private final String bootstrapServers;
  private final List<String> requiredTopics;
  private final int timeoutSeconds;

  public TopicAvailabilityCheck(
      String bootstrapServers, List<String> requiredTopics, int timeoutSeconds) {
    this.bootstrapServers = bootstrapServers;
    this.requiredTopics = requiredTopics;
    this.timeoutSeconds = timeoutSeconds;
  }

  @Override
  public String getName() {
    return "Topic Availability";
  }

  @Override
  public HealthCheckResult check() {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(timeoutSeconds * 1000));

    try (AdminClient adminClient = AdminClient.create(props)) {
      Set<String> existingTopics =
          adminClient.listTopics().names().get(timeoutSeconds, TimeUnit.SECONDS);

      List<String> missingTopics =
          requiredTopics.stream()
              .filter(t -> !existingTopics.contains(t))
              .collect(Collectors.toList());

      if (!missingTopics.isEmpty()) {
        return HealthCheckResult.degraded(
            getName(),
            "Missing topics: " + missingTopics,
            "Required: " + requiredTopics + " | Found: " + existingTopics);
      }

      Map<String, TopicDescription> descriptions =
          adminClient
              .describeTopics(requiredTopics)
              .allTopicNames()
              .get(timeoutSeconds, TimeUnit.SECONDS);

      StringBuilder detail = new StringBuilder();
      for (Map.Entry<String, TopicDescription> entry : descriptions.entrySet()) {
        List<TopicPartitionInfo> partitions = entry.getValue().partitions();
        int minReplicas = partitions.stream().mapToInt(p -> p.replicas().size()).min().orElse(0);
        detail.append(
            String.format(
                "%s — partitions: %d, min-replicas: %d; ",
                entry.getKey(), partitions.size(), minReplicas));
      }

      return HealthCheckResult.healthy(
          getName(), "All required topics exist — " + detail.toString().trim());

    } catch (Exception e) {
      return HealthCheckResult.unhealthy(
          getName(), "Failed to verify topic availability", e.getMessage());
    }
  }
}
