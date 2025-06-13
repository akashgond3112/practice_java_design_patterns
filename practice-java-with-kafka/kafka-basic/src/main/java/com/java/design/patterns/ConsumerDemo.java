package com.java.design.patterns;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConsumerDemo {

	private static final Logger log = LoggerFactory.getLogger(ConsumerDemo.class);

	public static void main(String[] args) {
		log.info("Starting Consumer demo");

		String groupId = "my-group";

		Properties properties = new Properties();

		// Connect to local Kafka setup
		properties.setProperty("bootstrap.servers", "localhost:9092");

		// create consumer configs
		properties.setProperty("key.deserializer", StringDeserializer.class.getName());
		properties.setProperty("value.deserializer", StringDeserializer.class.getName());

		properties.setProperty("group.id", groupId);
		properties.setProperty("auto.offset.reset", "earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

		//poll
		consumer.subscribe(List.of("demo_java"));

		while (true) {
			log.info("Polling");
			ConsumerRecords<String, String> records = consumer.poll(java.time.Duration.ofMillis(1000));
			for (ConsumerRecord<String, String> record : records) {
				log.info("key {}", record.key());
				log.info("value {}", record.value());
				log.info("partition {}", record.partition());
				log.info("offset {}", record.offset());
			}
		}
	}
}
