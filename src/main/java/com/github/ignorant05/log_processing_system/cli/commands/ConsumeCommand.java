package com.github.ignorant05.log_processing_system.cli.commands;

import java.util.concurrent.Callable;

import com.github.ignorant05.log_processing_system.service.ConsumeService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "consume", description = "Consume and display logs from Kafka")
public class ConsumeCommand implements Callable<Integer> {
  @Option(names = { "-b", "--bootstrap-servers" }, defaultValue = "kafka:9092", description = "Kafka bootstrap servers")
  private String bootstrapServers;

  @Option(names = { "-t", "--topic" }, defaultValue = "my-logs", description = "Kafka topic")
  private String topic;

  @Option(names = { "-g", "--group-id" }, defaultValue = "we-consumers", description = "Consumer Group ID")
  private String groupID;

  @Option(names = { "--from-beginning" }, description = "Reading from the begging of the topic")
  private boolean fromBeginning;

  @Override
  public Integer call() throws Exception {
    ConsumeService consumeService = new ConsumeService(bootstrapServers, topic, groupID, fromBeginning);
    return consumeService.consumeLogs();
  }
}
