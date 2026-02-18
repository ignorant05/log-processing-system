package com.github.ignorant05.log_processing_system.cli;

import com.github.ignorant05.log_processing_system.cli.commands.ConsumeCommand;
import com.github.ignorant05.log_processing_system.cli.commands.GenerateCommand;
import com.github.ignorant05.log_processing_system.cli.commands.TopicCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/** LogCLI */
@Command(
    name = "klog",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "Kafka log processing CLI tool",
    subcommands = {GenerateCommand.class, ConsumeCommand.class, TopicCommand.class})
public class LogCLI implements Runnable {

  @Override
  public void run() {
    System.out.println("Kafka Log Processing CLI tool");
    System.out.println("Use --help (or -h) to list the available commands");
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new LogCLI()).execute(args);
    System.exit(exitCode);
  }
}
