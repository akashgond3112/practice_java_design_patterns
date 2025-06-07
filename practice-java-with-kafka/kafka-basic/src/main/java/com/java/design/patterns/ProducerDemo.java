package com.java.design.patterns;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDemo {

	private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class);

	public static void main(String[] args) {
		log.info("Starting producer demo");

		Properties properties = new Properties();

		// Connect to local Kafka setup
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("key.serializer", StringSerializer.class.getName());
		properties.setProperty("value.serializer", StringSerializer.class.getName());

		// create producer
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		// create producer record
		ProducerRecord<String, String> producerRecord = new ProducerRecord<>("demo_java", "hellow world",
				"value1");

		// send data
		producer.send(producerRecord);

		// flush and close producer
		producer.flush();
		producer.close();

		log.info("Producer properties set for local Kafka setup");
	}
}
