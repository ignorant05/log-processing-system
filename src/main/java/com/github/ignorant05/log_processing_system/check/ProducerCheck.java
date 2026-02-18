package com.github.ignorant05.log_processing_system.check;

import com.github.ignorant05.log_processing_system.model.HealthCheck;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringSerializer;

/** ProducerCheck */
public class ProducerCheck implements HealthCheck {
  private final String bootstrapServers;
  private final String topic;
  private final int timeoutMS;

  public ProducerCheck(String bootstrapServers, String topic, int timeoutMS) {
    this.bootstrapServers = bootstrapServers;
    this.topic = topic;
    this.timeoutMS = timeoutMS;
  }

  @Override
  public String getName() {
    return "Producer Check";
  }

  @Override
  public HealthCheckResult check() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, String.valueOf(timeoutMS));
    props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMS));
    props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, String.valueOf(timeoutMS + 1000));

    try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
      List<PartitionInfo> partitions = producer.partitionsFor(topic);
      if (partitions.isEmpty() || partitions == null) {
        return HealthCheckResult.degraded(
            getName(), "Producer initialized but topic has no partitions: " + topic, null);
      }
      return HealthCheckResult.healthy(
          getName(),
          String.format(
              "Producer connected — topic '%s' has %d partition(s)", topic, partitions.size()));
    } catch (Exception e) {
      return HealthCheckResult.unhealthy(
          getName(), "Producer failed to connect to " + bootstrapServers, e.getMessage());
    }
  }
}
