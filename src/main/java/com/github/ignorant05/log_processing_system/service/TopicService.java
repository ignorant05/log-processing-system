package com.github.ignorant05.log_processing_system.service;

import com.github.ignorant05.log_processing_system.model.TopicAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
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

/** TopicService */
public class TopicService {
  private String bootstrapServers;
  private String topicName;
  private int partitions;
  private short replications;
  private int timeout;

  public TopicService(
      String bootstrapServers, String topicName, int partitions, short replications, int timeout) {
    this.bootstrapServers = bootstrapServers;
    this.topicName = topicName;
    this.partitions = partitions;
    this.replications = replications;
    this.timeout = timeout;
  }

  public int topicOps(TopicAction action) {
    System.out.println("Topic command: " + action);
    if (action != TopicAction.list && (topicName == null || topicName.isEmpty())) {
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
