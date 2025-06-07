package com.java.design.patterns;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDemoWithCallBack {

	private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithCallBack.class);

	public static void main(String[] args) {
		log.info("Starting producer demo");

		Properties properties = new Properties();

		// Connect to local Kafka setup
		properties.setProperty("bootstrap.servers", "localhost:9092");
		properties.setProperty("key.serializer", StringSerializer.class.getName());
		properties.setProperty("value.serializer", StringSerializer.class.getName());

		// create producer
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		for(int i=0; i < 10; i++){

			// create producer record
			ProducerRecord<String, String> producerRecord = new ProducerRecord<>("demo_java", "hello new world",
					"value"+i);

			// send data
			producer.send(producerRecord, new Callback() {
				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if (exception == null) {
						// record was sent successfully
						log.info("Received new metadata. \n" +
								"Topic: " + metadata.topic() + "\n" +
								"Partition: " + metadata.partition() + "\n" +
								"Offset: " + metadata.offset() + "\n" +
								"Timestamp: " + metadata.timestamp());
					} else {
						log.error("Error while producing", exception);
					}
				}
			});
		}
		

		// flush and close producer
		producer.flush();
		producer.close();

		log.info("Producer properties set for local Kafka setup");
	}
}
