package com.java.design.patterns;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithKeys {

	private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithKeys.class);

	public static void main(String[] args) {
		log.info("Starting producer demo");

		Properties properties = new Properties();

		// Connect to local Kafka setup
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("key.serializer", StringSerializer.class.getName());
		properties.setProperty("value.serializer", StringSerializer.class.getName());

		// create producer
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		for (int j = 0; j < 2; j++) {

			for (int i = 0; i < 10; i++) {

				String topic = "demo_java";
				String key = "key-" + i;
				String value = "value-" + i;

				// create producer record
				ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, key, value);

				// send data
				producer.send(producerRecord, (metadata, exception) -> {
					if (exception == null) {
						// record was sent successfully
						log.info("key : {} | value : {} | partition : {}", key, value, metadata.partition());
					} else {
						log.error("Error while producing", exception);
					}
				});
			}
		}


		// flush and close producer
		producer.flush();
		producer.close();

		log.info("Producer properties set for local Kafka setup");
	}
}
