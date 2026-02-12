package com.github.ignorant05.log_processing_system.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.ignorant05.log_processing_system.generator.LogGenerator;
import com.github.ignorant05.log_processing_system.kafka.consumer.LogConsumer;
import com.github.ignorant05.log_processing_system.kafka.producer.LogProducer;
import com.github.ignorant05.log_processing_system.model.LogEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.TopicExistsException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** LogCLI */
@Command(
    name = "klog",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "Kafka log processing CLI tool",
    subcommands = {GenerateCommand.class, ConsumeCommand.class, Topic.class})
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

@Command(name = "generate", description = "Generate and send logs to Kafka")
class GenerateCommand implements Callable<Integer> {

  public static final int DEFAULT_WAITING_TIME_IN_MILLISECONDS = 2000;

  @Option(
      names = {"-b", "--bootstrap-servers"},
      defaultValue = "localhost:9095",
      description = "Kafka bootstrap servers")
  private String bootstrapServers;

  @Option(
      names = {"-t", "--topic"},
      defaultValue = "my-logs",
      description = "Kafka topic")
  private String topic;

  @Option(
      names = {"-i", "--interval"},
      defaultValue = "1000",
      description = "Interval between logs in \"Milliseconds\"")
  private long interval;

  @Option(
      names = {"-c", "--count"},
      defaultValue = "0",
      description = "Number of logs to generate (0 is infinite)")
  private int count;

  @Option(
      names = {"-r", "--rate"},
      description = "Number of logs per second (overrides \"count\")")
  private Integer rate;

  @Option(
      names = {"-s", "--sync"},
      description = "Use synchronous sending mode (default is \"Asynchronous mode\")")
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

    long actualInterval = interval;
    if (rate != null && rate > 0) {
      actualInterval = 1000 / rate;
      System.out.println("\tActual Interval: " + actualInterval + "ms");
    }

    try (LogProducer producer = new LogProducer(bootstrapServers, topic)) {
      int generated = 0;
      while (count == 0 || generated < count) {
        LogEntry logEntry = LogGenerator.generateRandomLog();

        try {
          if (isSync) producer.sendSync(logEntry);
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

      if (isSync) {
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

@Command(name = "consume", description = "Consume and display logs from Kafka")
class ConsumeCommand implements Callable<Integer> {
  @Option(
      names = {"-b", "--bootstrap-servers"},
      defaultValue = "localhost:9095",
      description = "Kafka bootstrap servers")
  private String bootstrapServers;

  @Option(
      names = {"-t", "--topic"},
      defaultValue = "my-logs",
      description = "Kafka topic")
  private String topic;

  @Option(
      names = {"-g", "--group-id"},
      defaultValue = "we-consumers",
      description = "Consumer Group ID")
  private String groupID;

  @Option(
      names = {"--from-beginning"},
      description = "Reading from the begging of the topic")
  private boolean fromBeginning;

  @Override
  public Integer call() throws Exception {
    try (LogConsumer consumer = new LogConsumer(bootstrapServers, groupID, topic)) {
      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    System.out.println("Shutting down...");
                    consumer.stop();
                  }));

      consumer.consume();
      return 0;

    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
    }

    return 1;
  }
}

@Command(name = "topic", description = "Kafka topic operations")
class Topic implements Callable<Integer> {
  @Option(
      names = {"-b", "--bootstrap-servers"},
      defaultValue = "localhost:9095",
      description = "Kafka bootstrap servers")
  private String bootstrapServers;

  @Option(
      names = {"-n", "--name"},
      defaultValue = "my-topic",
      description = "Target topic name")
  private String topicName;

  @Option(
      names = {"-p", "--partitions"},
      defaultValue = "1",
      description =
          "Target topic partitions (default is \"1\"; one topic contains at least one partition)")
  private int partitions;

  @Option(
      names = {"-r", "--replications"},
      defaultValue = "1",
      description = "How manay replications should exist for a partition (default is \"1\"")
  private short replications;

  @Option(
      names = {"--timeout"},
      defaultValue = "30",
      description = "Operation timeout in seconds (default: ${DEFAULT-VALUE})")
  private int timeout;

  @Parameters(index = "0", description = "Action: create, list, describe, delete")
  private Action action;

  public enum Action {
    create,
    list,
    describe,
    delete
  }

  @Override
  public Integer call() throws Exception {
    System.out.println("Topic command: " + action);
    if (action != Action.list && (topicName == null || topicName.isEmpty())) {
      System.err.println("Error: --name flag is required for " + action + " action");
      return 1;
    }

    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(AdminClientConfig.CLIENT_ID_CONFIG, "kafka-log-cli-topic-admin");
    props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, timeout * 1000);

    System.out.printf("Connecting to Kafka at %s...%n", bootstrapServers);

    try (Admin admin = Admin.create(props)) {
      switch (action) {
        case create:
          return createTopic(admin);
        case list:
          return fetchingTopicList(admin);
        case describe:
          return describeTopic(admin);
        case delete:
          return deleteTopic(admin);
        default:
          System.err.println(
              "Unknown action: "
                  + action
                  + "\nPlease make sure to select one of these: \"create\", \"list\", \"describe\", \"delete\"");
          return 1;
      }

    } catch (ExecutionException e) {
      System.err.println("Error: " + e.getMessage());
      if (e.getCause() != null) {
        if (e.getCause() instanceof TopicExistsException) {
          System.err.printf("Topic %s already exists!%n", topicName);
          return 1;
        }

        System.err.printf("Error: %s%n", e.getCause().getMessage());
      }
      return 1;
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      if (e.getCause() != null) {
        System.err.printf("Cause: %s%n", e.getCause().getMessage());
      }
      e.printStackTrace();
      return 1;
    }
  }

  private Integer createTopic(Admin admin) throws Exception {
    System.out.printf("Creating Topic: %s%n", topicName);
    System.out.printf("Partitions: %d%n", partitions);
    System.out.printf("Replications : %d%n", replications);

    ListTopicsResult topics = admin.listTopics();
    if (topics.names().get(timeout, TimeUnit.SECONDS).contains(topicName)) {
      System.err.printf(
          "Topic already exists with name: %s\nPlease chose another name for your topic...%n",
          topicName);
      return 1;
    }

    NewTopic newTopic = new NewTopic(topicName, partitions, replications);

    CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));

    KafkaFuture<Void> future = result.values().get(topicName);
    future.get(timeout, TimeUnit.SECONDS);

    System.out.printf("Topic %s has been successfully created!%n", topicName);

    return 0;
  }

  private Integer fetchingTopicList(Admin admin) throws Exception {
    System.out.println("Fetching Topic list...");

    ListTopicsResult topics = admin.listTopics();

    List<String> topicsList = new ArrayList<>(topics.names().get());

    if (topicsList.isEmpty()) {
      System.out.println("No topics found!");
      return 0;
    }

    Collections.sort(topicsList);

    System.out.println("┌─────────────────────────────────────────────────┐");
    System.out.println("\n├─────────────── Available Topics ───────────────┤");
    System.out.println("├─────────────────────────────────────────────────┤");
    System.out.println("┌─────────────────────────────────────────────────┐");

    for (int i = 0; i < topicsList.size(); i++) {

      String topicName = topicsList.get(i);
      if (topicName.startsWith("__"))
        System.out.printf("|| %2d. %s (internal)%n", i + 1, topicName);
      else System.out.printf("|| %2d. %s%n", i + 1, topicName);
    }

    System.out.println("├─────────────────────────────────────────────────┤");
    System.out.printf("Total: %d topics%n", topicsList.size());

    return 0;
  }

  private Integer describeTopic(Admin admin) throws Exception {
    System.out.printf("Describing Topic: %s%n", topicName);

    DescribeTopicsResult result = admin.describeTopics(Collections.singletonList(topicName));

    TopicDescription description =
        result.topicNameValues().get(topicName).get(timeout, TimeUnit.SECONDS);

    System.out.println("\nTopic Details:");
    System.out.println("┌─────────────────────────────────────────────────┐");
    System.out.printf("│ Name:          %-36s │%n", description.name());
    System.out.printf("│ Internal:      %-36s │%n", description.isInternal());
    System.out.printf("│ Partitions:    %-36d │%n", description.partitions().size());
    System.out.println("├─────────────────────────────────────────────────┤");

    System.out.println("│ Partition Details:                              │");
    for (TopicPartitionInfo partition : description.partitions()) {
      System.out.printf("│\tPartition: %d %n", partition.partition());
      System.out.printf("│\tLeader: %s%n", partition.leader());

      if (!partition.replicas().isEmpty()) {
        System.out.printf("│\tReplicas: ");
        for (Node node : partition.replicas()) {
          System.out.printf("%s:%d", node.host(), node.port());
        }
        System.out.println();
      }

      if (!partition.isr().isEmpty()) {
        System.out.printf("│\tIn-Sync Replicas: ");
        for (Node node : partition.replicas()) {
          System.out.printf("%s:%d", node.host(), node.port());
        }
        System.out.println();
      }

      if (partition.partition() < description.partitions().size() - 1)
        System.out.println("│                                      │");
    }

    System.out.println("├─────────────────────────────────────────────────┤");

    return 0;
  }

  private Integer deleteTopic(Admin admin) throws Exception {
    System.out.printf("Deleting Topic: %s%n", topicName);
    System.out.print("Are you sure? (y/N): ");

    Scanner scanner = new Scanner(System.in);
    String permit = scanner.nextLine().trim().toLowerCase();
    scanner.close();

    if (!permit.equals("y") && !permit.equals("yes")) {
      System.out.println("Operation Canceled!");
      return 0;
    }

    DeleteTopicsResult result = admin.deleteTopics(Collections.singletonList(topicName));

    result.topicNameValues().get(topicName).get(timeout, TimeUnit.SECONDS);

    System.out.printf("Topic %s had been deleted%n", topicName);
    return 0;
  }
}
