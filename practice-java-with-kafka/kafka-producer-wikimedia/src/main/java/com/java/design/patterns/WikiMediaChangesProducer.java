package com.java.design.patterns;

import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.EventHandler;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Properties;

public class WikiMediaChangesProducer {

	private static final Logger log = LoggerFactory.getLogger(WikiMediaChangesProducer.class);

	public static void main(String[] args) {
		log.info("Starting Consumer demo");

		// Load the logging configuration file
		System.setProperty("org.slf4j.simpleLogger.properties", "src/main/resources/simplelogger.properties");

		Properties properties = new Properties();

		// Connect to local Kafka setup
		properties.setProperty("bootstrap.servers", "localhost:9092");

		// create consumer configs
		properties.setProperty("key.serializer", StringSerializer.class.getName());
		properties.setProperty("value.serializer", StringSerializer.class.getName());

		// create producer
		KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

		String topic = "wikimedia.recentchange";

		EventHandler eventHandler = new WikiMediaChangeHandler(producer, topic);

		String url = "https://stream.wikimedia.org/v2/stream/recentchange";
		EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));
		EventSource eventSource = builder.build();

		//start the producer in other thread
		eventSource.start();

		// we produce for 5 minutes
		try {
			Thread.sleep(300000); // 5 minutes
		} catch (InterruptedException e) {
			log.error("Thread interrupted", e);
		} finally {
			eventSource.close();
			producer.close();
			log.info("Producer closed");
		}
	}


}
