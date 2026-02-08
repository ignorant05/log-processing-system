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
}
