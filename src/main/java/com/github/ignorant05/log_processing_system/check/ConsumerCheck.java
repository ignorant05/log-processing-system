package com.github.ignorant05.log_processing_system.check;

import com.github.ignorant05.log_processing_system.model.HealthCheck;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;

/** ConsumerCheck */
public class ConsumerCheck implements HealthCheck {
  private final String bootstrapServers;
  private final String groupID;
  private final String topic;
  private final int timeoutMS;

  public ConsumerCheck(String bootstrapServers, String groupID, String topic, int timeoutMS) {
    this.bootstrapServers = bootstrapServers;
    this.groupID = groupID;
    this.topic = topic;
    this.timeoutMS = timeoutMS;
  }

  @Override
  public String getName() {
    return "Consumer Check";
  }

  @Override
  public HealthCheckResult check() {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMS));
    props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(Math.min(timeoutMS, 10000)));
    props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMS));
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
      List<PartitionInfo> partitions = consumer.partitionsFor(topic);
      if (partitions.isEmpty() || partitions == null) {
        return HealthCheckResult.degraded(
            getName(),
            "Consumer initialized - topic " + topic + " has no partitions",
            "Group: " + groupID);
      }
      return HealthCheckResult.healthy(
          getName(),
          String.format(
              "CONNECTED — group: %s, topic: '%s', partitions visible: %d",
              groupID, topic, partitions.size()));
    } catch (Exception e) {
      return HealthCheckResult.unhealthy(
          getName(),
          "Consumer NOT RUNNING — could not connect (group: " + groupID + ")",
          e.getMessage());
    }
  }
}
