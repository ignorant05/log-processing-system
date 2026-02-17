package com.github.ignorant05.log_processing_system.service;

import com.github.ignorant05.log_processing_system.kafka.consumer.LogConsumer;

/**
 * ConsumeService
 */
public class ConsumeService {
	private String bootstrapServers;
	private String topic;
	private String groupID;
	private boolean fromBeginning;

	public ConsumeService(String bootstrapServers, String topic, String groupID, boolean fromBeginning) {
		this.bootstrapServers = bootstrapServers;
		this.topic = topic;
		this.groupID = groupID;
		this.fromBeginning = fromBeginning;
	}

	public int consumeLogs() {
		try (LogConsumer consumer = new LogConsumer(bootstrapServers, groupID, topic, fromBeginning)) {
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
