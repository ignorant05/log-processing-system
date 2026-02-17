package com.github.ignorant05.log_processing_system.cli.commands;

import java.util.concurrent.Callable;

import com.github.ignorant05.log_processing_system.model.TopicAction;
import com.github.ignorant05.log_processing_system.service.TopicService;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "topic", description = "Kafka topic operations")
public class TopicCommand implements Callable<Integer> {
  @Option(names = { "-b", "--bootstrap-servers" }, defaultValue = "kafka:9092", description = "Kafka bootstrap servers")
  private String bootstrapServers;

  @Option(names = { "-n", "--name" }, defaultValue = "my-topic", description = "Target topic name")
  private String topicName;

  @Option(names = { "-p",
      "--partitions" }, defaultValue = "1", description = "Target topic partitions (default is \"1\"; one topic contains at least one partition)")
  private int partitions;

  @Option(names = { "-r",
      "--replications" }, defaultValue = "1", description = "How manay replications should exist for a partition (default is \"1\"")
  private short replications;

  @Option(names = {
      "--timeout" }, defaultValue = "30", description = "Operation timeout in seconds (default: ${DEFAULT-VALUE})")
  private int timeout;

  @Parameters(index = "0", description = "Action: create, list, describe, delete")
  private TopicAction action;

  @Override
  public Integer call() throws Exception {
    TopicService topicService = new TopicService(bootstrapServers, topicName, partitions, replications, timeout);

    return topicService.topicOps(action);
  }
}
