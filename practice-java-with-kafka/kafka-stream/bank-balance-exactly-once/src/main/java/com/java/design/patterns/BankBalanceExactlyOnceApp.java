package com.java.design.patterns;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;

import java.time.Instant;
import java.util.Properties;

public class BankBalanceExactlyOnceApp {

	public static final String COUNT = "count";
	public static final String BALANCE = "balance";

	public static void main(String[] args) {
		Properties config = getProperties();

		// json Serde
		final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
		final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
		final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);

		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, JsonNode> bankTransactions = builder.stream("bank-transactions",
				Consumed.with(Serdes.String(), jsonSerde));


		// create the initial json object for balances
		ObjectNode initialBalance = JsonNodeFactory.instance.objectNode();
		initialBalance.put(COUNT, 0);
		initialBalance.put(BALANCE, 0);
		initialBalance.put("time", Instant.ofEpochMilli(0L).toString());

		KTable<String, JsonNode> bankBalance = bankTransactions.groupByKey(Grouped.with(Serdes.String(), jsonSerde))
				.aggregate(() -> initialBalance, (key, transaction, balance) -> newBalance(transaction, balance),
						Materialized.<String, JsonNode, KeyValueStore<Bytes, byte[]>> as("bank-balance-agg")
								.withKeySerde(Serdes.String()).withValueSerde(jsonSerde));

		bankBalance.toStream().to("bank-balance-exactly-once", Produced.with(Serdes.String(), jsonSerde));

		try (KafkaStreams streams = new KafkaStreams(builder.build(), config)) {
			streams.cleanUp();
			streams.start();

			// print the topology
			streams.metadataForLocalThreads().forEach(System.out::println);

			// shutdown hook to correctly close the streams application
			Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
		}
	}

	private static Properties getProperties() {
		Properties config = new Properties();

		config.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-balance-application");
		config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
		config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// we disable the cache to demonstrate all the "steps" involved in the transformation - not recommended in prod
		config.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 0);

		// Exactly once processing!!
		config.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
		return config;
	}

	private static JsonNode newBalance(JsonNode transaction, JsonNode balance) {
		// create a new balance json object
		ObjectNode newBalance = JsonNodeFactory.instance.objectNode();
		newBalance.put(COUNT, balance.get(COUNT).asInt() + 1);
		newBalance.put(BALANCE, balance.get(BALANCE).asInt() + transaction.get("amount").asInt());

		long balanceEpoch = Instant.parse(balance.get("time").asText()).toEpochMilli();
		long transactionEpoch = Instant.parse(transaction.get("time").asText()).toEpochMilli();
		Instant newBalanceInstant = Instant.ofEpochMilli(Math.max(balanceEpoch, transactionEpoch));
		newBalance.put("time", newBalanceInstant.toString());
		return newBalance;
	}
}
