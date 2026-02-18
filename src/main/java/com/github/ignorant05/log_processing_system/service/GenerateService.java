package com.github.ignorant05.log_processing_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ignorant05.log_processing_system.generator.LogGenerator;
import com.github.ignorant05.log_processing_system.kafka.producer.LogProducer;
import com.github.ignorant05.log_processing_system.model.LogEntry;
import java.util.concurrent.ExecutionException;

/** GenerateService */
public class GenerateService {
  private String bootstrapServers;
  private String topic;
  private long interval;
  private int count;
  private Integer rate;
  private boolean isSync;

  public GenerateService(
      String bootstrapServers,
      String topic,
      long interval,
      int count,
      Integer rate,
      boolean isSync) {
    this.bootstrapServers = bootstrapServers;
    this.topic = topic;
    this.interval = interval;
    this.count = count;
    this.rate = rate;
    this.isSync = isSync;
  }

  public static final int DEFAULT_WAITING_TIME_IN_MILLISECONDS = 2000;

  public int generateLogs() {

    long actualInterval = this.interval;
    if (this.rate != null && this.rate > 0) {
      actualInterval = 1000 / this.rate;
      System.out.println("\tActual Interval: " + actualInterval + "ms");
    }

    try (LogProducer producer = new LogProducer(this.bootstrapServers, this.topic)) {
      int generated = 0;
      while (this.count == 0 || generated < this.count) {
        LogEntry logEntry = LogGenerator.generateRandomLog();

        try {
          if (this.isSync) producer.sendSync(logEntry);
          else producer.sendAsync(logEntry);

          generated++;
          if (generated % 10 == 0) {
            System.out.printf("\tGenerated Logs: %d...%n " + generated);
          }

          if (actualInterval > 0) {
            Thread.sleep(actualInterval);
          }

        } catch (JsonProcessingException e) {
          System.err.println("Failed to serialize Log: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
          System.err.println("Failed to send Log: " + e.getMessage());
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }

      if (this.isSync) {
        producer.flush();
        System.out.println("Flushing remaining messages...");
        Thread.sleep(DEFAULT_WAITING_TIME_IN_MILLISECONDS);
      }

      System.out.println("Log Generation Completed");
      return 0;

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }

    return 1;
  }
}
