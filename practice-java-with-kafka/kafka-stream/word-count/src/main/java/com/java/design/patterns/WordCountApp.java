package com.java.design.patterns;

import java.util.Properties;
import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordCountApp {

	static Logger logger = LoggerFactory.getLogger(WordCountApp.class);

	public Topology createTopology() {
		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> textLines = builder.stream("word-count-input");

		KTable<String, Long> wordCounts = textLines.mapValues(
						textLine -> textLine == null ? "" : textLine.toLowerCase())
				.flatMapValues(textLine -> Arrays.asList(textLine.split("\\W+")))
				.filter((key, word) -> word != null && !word.isEmpty()).groupBy((key, word) -> word)
				.count(Materialized.as("Counts"));

		wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

		return builder.build();
	}

	public static void main(String[] args) {
		Properties config = new Properties();
		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		WordCountApp wordCountApp = new WordCountApp();

		try (KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), config)) {
			// Add shutdown hook before starting the streams
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				logger.info("Shutting down the Kafka Streams application");
				streams.close();
			}));
			
			// Start the streams
			streams.start();
			logger.info("Kafka Streams started");

			// Print topology information periodically
			int count = 0;
			while (count < 10) { // Only run for 10 iterations for testing
				streams.metadataForLocalThreads().forEach(data -> 
					logger.info("Thread metadata: {}", data));
				try {
					Thread.sleep(5000);
					count++;
				} catch (InterruptedException e) {
					logger.error("Interrupted while sleeping", e);
					Thread.currentThread().interrupt(); // Restore the interrupted status
					break;
				}
			}
			
			logger.info("Application completed successfully");
		}
	}
}
