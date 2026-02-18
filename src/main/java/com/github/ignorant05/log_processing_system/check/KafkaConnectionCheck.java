package com.github.ignorant05.log_processing_system.check;

import com.github.ignorant05.log_processing_system.model.HealthCheck;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;

/** KafkaConnectionCheck */
public class KafkaConnectionCheck implements HealthCheck {
  private final String bootstrapServers;
  private final int timeoutSeconds;

  public KafkaConnectionCheck(String bootstrapServers, int timeoutSeconds) {
    this.bootstrapServers = bootstrapServers;
    this.timeoutSeconds = timeoutSeconds;
  }

  @Override
  public String getName() {
    return "Kafka Connection";
  }

  @Override
  public HealthCheckResult check() {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(timeoutSeconds * 1000));
    props.put(
        AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(timeoutSeconds * 1000));

    try (AdminClient adminClient = AdminClient.create(props)) {
      DescribeClusterResult cluster = adminClient.describeCluster();
      String clusterID = cluster.clusterId().get(timeoutSeconds, TimeUnit.SECONDS);
      Collection<?> nodes = cluster.nodes().get(timeoutSeconds, TimeUnit.SECONDS);

      if (nodes.isEmpty()) {
        return HealthCheckResult.unhealthy(
            getName(),
            "No Brokers Available!",
            "Cluster ID: " + clusterID + " — broker list is empty");
      }

      return HealthCheckResult.healthy(
          getName(),
          String.format("Connected — cluster ID: %s, brokers: %d", clusterID, nodes.size()));

    } catch (Exception e) {
      return HealthCheckResult.unhealthy(
          getName(), "Cannot reach Kafka broker at " + bootstrapServers, e.getMessage());
    }
  }
}
