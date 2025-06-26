package com.java.design.patterns;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;

import java.util.Properties;

public class StreamsStarterApp {

	public static void main(String[] args) {

		Logger logger = org.slf4j.LoggerFactory.getLogger(StreamsStarterApp.class);

		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> kStream = builder.stream("input-topic-name");
		kStream.to("word-count-output");

		try (KafkaStreams streams = new KafkaStreams(builder.build(), config)) {
			streams.cleanUp();
			streams.start();

			logger.info("Local thread metadata: {}", streams.metadataForLocalThreads());

			Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
		}

	}

}
