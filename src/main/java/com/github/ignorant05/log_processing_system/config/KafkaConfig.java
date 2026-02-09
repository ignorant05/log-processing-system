package com.github.ignorant05.log_processing_system.config;

import java.util.Properties;

/**
 * KafkaConfig
 */
public class KafkaConfig {

	private KafkaConfig() {
	}

	public static Properties getProducerConfig(String bootstrapServers) {
		Properties props = new Properties();

		props.put("bootstrap.servers", bootstrapServers);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("acks", "1");
		props.put("retries", 3);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432); // 32MB

		return props;
	}

	public static Properties getConsumerConfig(String bootstrapServers, String groupID) {
		Properties props = new Properties();

		props.put("bootstrap.servers", bootstrapServers);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("auto.offset.reset", "earliest");
		props.put("session.timeout.ms", "30000");

		return props;
	}
}
