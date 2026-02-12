package com.github.ignorant05.log_processing_system.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ignorant05.log_processing_system.model.LogEntry;
import com.github.ignorant05.log_processing_system.util.JsonUtil;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.clients.producer.*;

/** LogProducer */
public class LogProducer implements AutoCloseable {
  private final KafkaProducer<String, String> producer;
  private final String topic;
  private final AtomicLong messagesSent = new AtomicLong(0);
  private final AtomicLong messagesFailed = new AtomicLong(0);

  public LogProducer(String bootstrapServers, String topic) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("acks", "all");
    props.put("retries", 3);
    props.put("max.in.flight.requests.per.connection", 1);
    props.put("enable.idempotence", false);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // 16KB batches
    props.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms to batch
    props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // compression
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer

    this.producer = new KafkaProducer<>(props);
    this.topic = topic;
  }

  public void sendSync(LogEntry logEntry) throws Exception {
    String json = JsonUtil.toJson(logEntry);

    ProducerRecord<String, String> record = new ProducerRecord<>(topic, logEntry.getID(), json);

    try {
      RecordMetadata metadata = producer.send(record).get();

      messagesSent.incrementAndGet();
      if (messagesSent.get() % 100 == 0) {
        System.out.printf(
            "Sent %d messages to %s-%d (offset: %d)%n",
            messagesSent.get(), metadata.topic(), metadata.partition(), metadata.offset());
      }
    } catch (Exception e) {
      messagesFailed.incrementAndGet();
      System.err.printf("Failed to send message: %s%n", e.getCause().getMessage());
      throw e;
    }
  }

  public void sendAsync(LogEntry logEntry)
      throws JsonProcessingException, ExecutionException, InterruptedException {
    String json = JsonUtil.toJson(logEntry);

    ProducerRecord<String, String> record = new ProducerRecord<>(topic, logEntry.getID(), json);

    producer.send(
        record,
        (metadata, exception) -> {
          if (exception == null) {
            messagesSent.incrementAndGet();
          } else {
            messagesFailed.incrementAndGet();
            System.err.printf(
                "Failed to send message Asynchranously: %s%n", exception.getMessage());
          }
        });
  }

  public void flush() {
    producer.flush();
  }

  @Override
  public void close() {
    producer.close();

    System.out.printf("\nProducer Statistics: %n");
    System.out.printf("\tTotal messages sent: %d%n", messagesSent.get());
    System.out.printf("\tTotal messages failed: %d%n", messagesFailed.get());
    System.out.printf(
        "\tSuccess Rate: %.2f%%%n",
        (messagesSent.get() * 100.0) / (messagesSent.get() + messagesFailed.get()));
  }

  public long getSentMessages() {
    return messagesSent.get();
  }

  public long getFailedMessages() {
    return messagesFailed.get();
  }
}
