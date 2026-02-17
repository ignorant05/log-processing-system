package com.github.ignorant05.log_processing_system.cli.commands;

import java.util.concurrent.Callable;
import com.github.ignorant05.log_processing_system.service.GenerateService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "generate", description = "Generate and send logs to Kafka")
public class GenerateCommand implements Callable<Integer> {

  public static final int DEFAULT_WAITING_TIME_IN_MILLISECONDS = 2000;

  @Option(names = { "-b", "--bootstrap-servers" }, defaultValue = "kafka:9092", description = "Kafka bootstrap servers")
  private String bootstrapServers;

  @Option(names = { "-t", "--topic" }, defaultValue = "my-logs", description = "Kafka topic")
  private String topic;

  @Option(names = { "-i",
      "--interval" }, defaultValue = "1000", description = "Interval between logs in \"Milliseconds\"")
  private long interval;

  @Option(names = { "-c", "--count" }, defaultValue = "0", description = "Number of logs to generate (0 is infinite)")
  private int count;

  @Option(names = { "-r", "--rate" }, description = "Number of logs per second (overrides \"count\")")
  private Integer rate;

  @Option(names = { "-s", "--sync" }, description = "Use synchronous sending mode (default is \"Asynchronous mode\")")
  private boolean isSync;

  @Override
  public Integer call() throws Exception {
    System.out.println("Generating logs ...");
    System.out.println("\tBootstrap Servers: " + bootstrapServers);
    System.out.println("\tTopic: " + topic);
    System.out.println("\tInterval: " + interval + "ms");
    System.out.println("\tCount: " + count);
    System.out.println("\tRate: " + rate + " logs/second");
    System.out.println("\tMode: " + (isSync ? "Synchronous" : "Asynchronous"));

    GenerateService generateService = new GenerateService(bootstrapServers, topic, interval, count, rate, isSync);

    return generateService.generateLogs();
  }
}
