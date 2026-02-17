package com.github.ignorant05.log_processing_system.kafka.consumer;

import static org.fusesource.jansi.Ansi.ansi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ignorant05.log_processing_system.model.LogEntry;
import com.github.ignorant05.log_processing_system.util.JsonUtil;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

/** LogConsumer */
public class LogConsumer implements AutoCloseable {
  private final KafkaConsumer<String, String> consumer;
  private final String topic;
  private final AtomicLong messagesConsumed = new AtomicLong(0);
  private final AtomicLong messagesFailed = new AtomicLong(0);
  private volatile boolean isRunning = true;
  private volatile boolean fromBeginning = true;

  public LogConsumer(String bootstrapServers, String groupID, String topic, boolean fromBeginning) {
    Properties props = new Properties();
    props.setProperty("bootstrap.servers", bootstrapServers);
    props.setProperty("group.id", groupID);
    props.setProperty(
        "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty(
        "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    props.setProperty("enable.auto.commit", "true");
    props.setProperty("auto.commit.interval.ms", "1000");
    props.put("auto.offset.reset", "earliest");
    props.put("max.poll.records", 100);

    System.setProperty("jansi.force", "true");
    AnsiConsole.systemInstall();
    this.consumer = new KafkaConsumer<>(props);
    this.topic = topic;
    this.fromBeginning = fromBeginning;

    AnsiConsole.systemInstall();
  }

  public void consume() {
    consumer.subscribe(Collections.singleton(topic));

    try {
      if (fromBeginning) {
        consumer.poll(Duration.ofMillis(100));
        consumer.seekToBeginning(consumer.assignment());
        System.out.println("Seeking to beginning of all partitions");
      }

      while (isRunning) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

        for (ConsumerRecord<String, String> record : records) {
          processLog(record);
        }

        if (messagesConsumed.get() % 100 == 0 && messagesConsumed.get() > 0) {
          displayStats();
        }
      }
    } finally {
      consumer.close();
    }
  }

  public void stop() {
    isRunning = !isRunning;
  }

  @Override
  public void close() {
    stop();
    consumer.close();

    AnsiConsole.systemUninstall();
  }

  public void processLog(ConsumerRecord<String, String> record) {
    try {
      LogEntry logEntry = JsonUtil.fromJson(record.value());
      displayRecord(logEntry, record);
      messagesConsumed.incrementAndGet();
    } catch (JsonProcessingException e) {
      messagesFailed.incrementAndGet();
      System.err.printf(
          "Failed to process message at offset %d: %s%n", record.offset(), e.getMessage());
    } catch (Exception e) {
      messagesFailed.incrementAndGet();
      System.err.printf("Failed to process message %s%n", e.getMessage());
    }
  }

  public void displayRecord(LogEntry logEntry, ConsumerRecord<String, String> record) {
    String color = getLevelColor(logEntry.getLevel());
    String timestamp = logEntry.getTimestamp().toString().substring(11, 23);

    String prettyOutput = color
        + "["
        + timestamp
        + "]"
        + "\u001B[0m"
        + " \u001B[35m"
        + String.format("%-20s", logEntry.getService())
        + "\u001B[0m"
        + " "
        + color
        + String.format("%-7s", logEntry.getLevel())
        + "\u001B[0m"
        + " \u001B[36m"
        + logEntry.getMessage()
        + "\u001B[0m"
        + " (p:"
        + record.partition()
        + ", o:"
        + record.offset()
        + ")";

    System.out.println(prettyOutput);
  }

  public String getLevelColor(String level) {
    return switch (level) {
      case "INFO" -> "\u001B[32m"; // Green
      case "ERROR" -> "\u001B[31m"; // Red
      case "WARN" -> "\u001B[33m"; // Yellow
      case "DEBUG" -> "\u001B[34m"; // Blue
      default -> "\u001B[37m";
    };
  }

  public void displayStats() {
    System.out.println();
    System.out.println(
        ansi()
            .fg(Ansi.Color.WHITE)
            .a(
                String.format(
                    "Consumed %s | Failed: %d", messagesConsumed.get(), messagesFailed.get()))
            .reset());
    System.out.println();
  }

  public long getConsumedMessages() {
    return this.messagesConsumed.get();
  }

  public long getFailedMessages() {
    return this.messagesFailed.get();
  }
}
