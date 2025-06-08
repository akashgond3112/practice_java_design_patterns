package com.java.design.patterns;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class ConsumerDemoWithShutDown {

	private static final Logger log = LoggerFactory.getLogger(ConsumerDemoWithShutDown.class);

	public static void main(String[] args) {
		log.info("Starting Consumer demo");

		// Load the logging configuration file
		System.setProperty("org.slf4j.simpleLogger.properties", "src/main/resources/simplelogger.properties");

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

		// get reference to the current thread
		final Thread mainThread = Thread.currentThread();

		// add the shut-down hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.info("Shutting down");
				consumer.wakeup();

				// join the main thread to allow the execution of the code
				try {
					mainThread.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

			}
		});
		//poll
		try {
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
		} catch (WakeupException e) {
			log.info("Consumer is starting to Shutting down");
		} catch (Exception e) {
			log.error("Error", e);
		} finally {
			log.info("Shutting down gracefully");
			consumer.close();
		}
	}
}
