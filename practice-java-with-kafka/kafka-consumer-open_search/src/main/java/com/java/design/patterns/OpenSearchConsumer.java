package com.java.design.patterns;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Properties;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSearchConsumer {

	private static final Logger logger = LoggerFactory.getLogger(OpenSearchConsumer.class);

	/**
	 * Creates and configures an OpenSearch client
	 *
	 * @return a configured RestHighLevelClient
	 */
	public static RestHighLevelClient createOpenSearchClient() {
		String connString = "http://localhost:9200";

		RestHighLevelClient restHighLevelClient;
		URI connUri = URI.create(connString);
		String userInfo = connUri.getUserInfo();

		if (userInfo == null) {
			// No authentication required
			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort())));
		} else {
			// Authentication info present in the connection string (username:password
			// format)
			String[] auth = userInfo.split(":");
			String username = auth[0];
			char[] password = auth.length > 1 ? auth[1].toCharArray() : new char[0];

			CredentialsStore cp = new BasicCredentialsProvider();
			AuthScope authScope = new AuthScope(connUri.getHost(), connUri.getPort());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

			cp.setCredentials(authScope, credentials);

			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort())).setHttpClientConfigCallback(
							(HttpAsyncClientBuilder httpClientBuilder) -> httpClientBuilder.setDefaultCredentialsProvider(
									cp)));
		}

		return restHighLevelClient;
	}

	/**
	 * Main method for the OpenSearch consumer application. Implementation to be added for creating an OpenSearch
	 * client, configuring Kafka consumer, and consuming Kafka topics to index data in OpenSearch.
	 */
	public static void main(String[] args) throws IOException {

		RestHighLevelClient client = createOpenSearchClient();

		KafkaConsumer<String, String> consumer = createKafkaConsumer();


		// get reference to the current thread
		final Thread mainThread = Thread.currentThread();

		// add the shut-down hook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down");
				consumer.wakeup();

				// join the main thread to allow the execution of the code
				try {
					mainThread.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

			}
		});

		try (client; consumer) {
			// Use the low-level REST client to create the index without problematic
			// parameters
			Request indexExistsRequest = new Request("HEAD", "/wikimedia");
			boolean indexExists = checkIfIndexExists(client, indexExistsRequest);

			if (!indexExists) {
				// Use PUT method without the cluster_manager_timeout parameter
				Request createIndexRequest = new Request("PUT", "/wikimedia");
				// Use master_timeout instead of cluster_manager_timeout
				createIndexRequest.addParameter("timeout", "30s");
				createIndexRequest.addParameter("master_timeout", "30s");

				Response response = client.getLowLevelClient().performRequest(createIndexRequest);
				logger.info("Index created successfully. Status: {}", response.getStatusLine().getStatusCode());
			} else {
				logger.info("Index already exists.");
			}

			// Subscribe to the topic
			consumer.subscribe(java.util.Collections.singletonList("wikimedia.recentchange"));

			BulkRequest bulkRequest = new BulkRequest();

			while (true) {
				logger.info("Polling for messages from Kafka...");
				BulkRequest finalBulkRequest = bulkRequest;
				consumer.poll(Duration.ofMillis(3000)).forEach(record -> {
					logger.debug("Received message: key={}, value={}, partition={}, offset={}", record.key(),
							record.value(), record.partition(), record.offset());

					String id = extractIdFromRecord(record.value());

					IndexRequest indexRequest = new IndexRequest("wikimedia").id(id)
							.source(record.value(), XContentType.JSON);

					finalBulkRequest.add(indexRequest);

					/*try {
						IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);

						logger.debug("Indexed document with ID: {}, Version: {}, Result: {}", index.getId(),
								index.getVersion(), index.getResult());
					} catch (Exception e) {
						e.printStackTrace();
					}*/
				});

				if (bulkRequest.numberOfActions() > 0) {
					try {
						BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
						if (bulkResponse.hasFailures()) {
							logger.error("Bulk indexing failed: {}", bulkResponse.buildFailureMessage());
						} else {
							logger.info("Bulk indexing successful. Indexed {} documents.",
									bulkResponse.getItems().length);
							Thread.sleep(1000); // Sleep for 1 second after successful bulk indexing
						}
					} catch (IOException | InterruptedException e) {
						logger.error("Error during bulk indexing: ", e);
					}

					// Commit offsets after processing messages
					consumer.commitSync();
					logger.info("Offsets committed successfully.");

					// Reset the bulk request after processing
					bulkRequest = new BulkRequest();
				}
			}
		} catch (WakeupException e) {
			logger.info("Consumer is starting to Shutting down");
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			logger.info("Shutting down gracefully");
			try {
				client.close();
				consumer.close();
			} catch (IOException e) {
				logger.error("Error closing OpenSearch client: ", e);
			}

			client.close();
		}
	}

	private static String extractIdFromRecord(String value) {
		// Assuming the value is a JSON string and contains an "id" field
		return com.google.gson.JsonParser.parseString(value).getAsJsonObject().get("meta").getAsJsonObject().get("id")
				.getAsString(); // Assuming the value is a JSON string with an "id" field
	}

	/**
	 * Checks if the specified index exists in OpenSearch.
	 *
	 * @param client
	 * 		the RestHighLevelClient
	 * @param indexExistsRequest
	 * 		the Request to check index existence
	 * @return true if the index exists, false otherwise
	 */
	private static boolean checkIfIndexExists(RestHighLevelClient client, Request indexExistsRequest) {
		boolean indexExists = false;
		try {
			Response response = client.getLowLevelClient().performRequest(indexExistsRequest);
			indexExists = response.getStatusLine().getStatusCode() == 200;
		} catch (IOException e) {
			// Index does not exist, will create it
			logger.warn("Index check failed: {}", e.getMessage());
		}
		return indexExists;
	}

	private static KafkaConsumer<String, String> createKafkaConsumer() {
		// Implementation for creating and configuring a Kafka consumer
		// This method should return a configured KafkaConsumer instance
		// For now, returning null as a placeholder

		logger.info("Starting Consumer demo");

		String groupId = "consumer_open_search";

		Properties properties = new Properties();

		// Connect to local Kafka setup
		properties.setProperty("bootstrap.servers", "localhost:9092");

		// create consumer configs
		properties.setProperty("key.deserializer", StringDeserializer.class.getName());
		properties.setProperty("value.deserializer", StringDeserializer.class.getName());

		properties.setProperty("group.id", groupId);
		properties.setProperty("auto.offset.reset", "latest");

		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		return new KafkaConsumer<>(properties);
	}
}
