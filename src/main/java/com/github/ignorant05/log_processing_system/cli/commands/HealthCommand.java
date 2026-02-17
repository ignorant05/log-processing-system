package com.github.ignorant05.log_processing_system.cli.commands;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "health", description = "Checks if:\n"
    + "- Kafka broker reachable.\n"
    + "- Topic exists.\n"
    + "- Producer status.\n"
    + "- Consumer status.\n"
    + "- brokers available.\n"
    + "- partitions available.\n"
    + "- replication info.\n"
    + "- topic availability check.\n"
    + "- producer/consumer status check.")
public class HealthCommand implements Callable<Integer> {

  @Override
  public Integer call() throws Exception {
    return 1;
  }
}
