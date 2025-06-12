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
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenSearchConsumer is responsible for consuming data from Kafka topics
 * and indexing it into OpenSearch.
 *
 * This class implements a Kafka consumer that reads events from the 'wikimedia.recentchange'
 * topic and indexes them into an OpenSearch index called 'wikimedia'. It uses
 * bulk processing for efficient indexing.
 *
 * The consumer handles graceful shutdown and includes error handling for
 * various failure scenarios.
 */
public class OpenSearchConsumer {

	public static final String CONN_STRING = "http://localhost:9200";
	private static final Logger logger = LoggerFactory.getLogger(OpenSearchConsumer.class);
	private static final String WIKIMEDIA_INDEX = "wikimedia";
	private static final String WIKIMEDIA_TOPIC = "wikimedia.recentchange";
	private static final int POLL_TIMEOUT_MS = 3000;

	/**
	 * Creates and configures an OpenSearch client with optional authentication.
	 *
	 * This method sets up a RestHighLevelClient to connect to OpenSearch.
	 * If authentication information is present in the connection string,
	 * it will configure the client with the provided credentials.
	 *
	 * @return a configured RestHighLevelClient for OpenSearch
	 */
	public static RestHighLevelClient createOpenSearchClient() {
		logger.info("Creating OpenSearch client with connection to {}", CONN_STRING);

		RestHighLevelClient restHighLevelClient;
		URI connUri = URI.create(CONN_STRING);
		String userInfo = connUri.getUserInfo();

		if (userInfo == null) {
			logger.info("Creating OpenSearch client without authentication");
			// No authentication required
			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort())));
		} else {
			logger.info("Creating OpenSearch client with authentication");
			// Authentication info present in the connection string (username:password format)
			String[] auth = userInfo.split(":");
			String username = auth[0];
			char[] password = auth.length > 1 ? auth[1].toCharArray() : new char[0];

			CredentialsStore cp = new BasicCredentialsProvider();
			AuthScope authScope = new AuthScope(connUri.getHost(), connUri.getPort());
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

			cp.setCredentials(authScope, credentials);

			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort()))
							.setHttpClientConfigCallback(httpClientBuilder ->
								httpClientBuilder.setDefaultCredentialsProvider(cp)));
		}
		logger.info("OpenSearch client created successfully");
		return restHighLevelClient;
	}

	/**
	 * Main method for the OpenSearch consumer application.
	 *
	 * This method orchestrates the Kafka consumer workflow:
	 * 1. Sets up OpenSearch and Kafka clients
	 * 2. Configures graceful shutdown
	 * 3. Creates the index if it doesn't exist
	 * 4. Consumes records from Kafka
	 * 5. Indexes the records in OpenSearch using bulk operations
	 * 6. Handles errors and resource cleanup
	 *
	 * @param args command line arguments (not used)
	 * @throws IOException if there's an error with client operations
	 */
	public static void main(String[] args) throws IOException {
	    logger.info("Starting OpenSearch Consumer application");

	    // Create the clients
	    RestHighLevelClient openSearchClient = createOpenSearchClient();
	    KafkaConsumer<String, String> kafkaConsumer = createKafkaConsumer();

	    // Configure graceful shutdown with a shutdown hook
	    final Thread mainThread = Thread.currentThread();
	    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        logger.info("Detected shutdown signal, starting graceful shutdown");
	        kafkaConsumer.wakeup();

	        try {
	            mainThread.join();
	            logger.info("Application has shut down");
	        } catch (InterruptedException e) {
	            logger.error("Shutdown interrupted: {}", e.getMessage());
	        }
	    }));

	    try (openSearchClient; kafkaConsumer) {
	        // Set up OpenSearch index
	        setupOpenSearchIndex(openSearchClient);

	        // Subscribe to the Kafka topic
	        logger.info("Subscribing to Kafka topic: {}", WIKIMEDIA_TOPIC);
	        kafkaConsumer.subscribe(java.util.Collections.singletonList(WIKIMEDIA_TOPIC));

	        // Process Kafka messages and index them in OpenSearch
	        processMessages(openSearchClient, kafkaConsumer);

	    } catch (WakeupException e) {
	        logger.info("Consumer is shutting down gracefully");
	    } catch (Exception e) {
	        logger.error("Unexpected error in consumer application", e);
	    } finally {
	        logger.info("Resources are being cleaned up");
	        try {
	            openSearchClient.close();
	            kafkaConsumer.close();
	            logger.info("All resources closed successfully");
	        } catch (IOException e) {
	            logger.error("Error while closing resources", e);
	        }
	    }
	}

	/**
	 * Sets up the OpenSearch index if it doesn't already exist.
	 *
	 * @param client the OpenSearch client
	 * @throws IOException if there's an error communicating with OpenSearch
	 */
	private static void setupOpenSearchIndex(RestHighLevelClient client) throws IOException {
	    // Check if index exists
	    Request indexExistsRequest = new Request("HEAD", "/" + WIKIMEDIA_INDEX);
	    boolean indexExists = checkIfIndexExists(client, indexExistsRequest);

	    if (!indexExists) {
	        // Create the index
	        Request createIndexRequest = new Request("PUT", "/" + WIKIMEDIA_INDEX);
	        createIndexRequest.addParameter("timeout", "30s");
	        createIndexRequest.addParameter("master_timeout", "30s");

	        Response response = client.getLowLevelClient().performRequest(createIndexRequest);
	        logger.info("Index created successfully. Status: {}", response.getStatusLine().getStatusCode());
	    } else {
	        logger.info("Index already exists.");
	    }
	}

	/**
	 * Processes messages from Kafka and indexes them in OpenSearch.
	 *
	 * @param client the OpenSearch client
	 * @param consumer the Kafka consumer
	 */
	private static void processMessages(RestHighLevelClient client, KafkaConsumer<String, String> consumer) {
	    BulkRequest bulkRequest = new BulkRequest();

	    // Add a flag to control the loop
	    boolean keepProcessing = true;
	    int emptyPolls = 0;
	    int maxEmptyPolls = 5; // Consider stopping after several empty polls, if desired

	    while (keepProcessing) {
	        logger.info("Polling for messages from Kafka...");

	        // Poll for records and track if we received any
	        var records = consumer.poll(Duration.ofMillis(POLL_TIMEOUT_MS));
	        boolean receivedRecords = !records.isEmpty();

	        if (receivedRecords) {
	            // Reset empty poll counter when we receive records
	            emptyPolls = 0;

	            // Process the records
	            records.forEach(record -> {
	                try {
	                    // Extract ID and create index request
	                    String id = extractIdFromRecord(record.value());

	                    // For debugging only, avoid logging full message in production
	                    logger.debug("Processing record: topic={}, partition={}, offset={}, id={}",
	                        record.topic(), record.partition(), record.offset(), id);

	                    // Create and add the index request to bulk operation
	                    IndexRequest indexRequest = new IndexRequest(WIKIMEDIA_INDEX)
	                        .id(id)
	                        .source(record.value(), XContentType.JSON);

	                    bulkRequest.add(indexRequest);
	                } catch (Exception e) {
	                    logger.error("Error processing record: {}", e.getMessage());
	                }
	            });

	            // Process bulk request if we have actions
	            processBulkRequest(client, consumer, bulkRequest);
	        } else {
	            // No records received in this poll
	            emptyPolls++;
	            logger.info("No records received. Empty poll count: {}/{}", emptyPolls, maxEmptyPolls);

	            // Optional: break the loop after several empty polls (useful for testing)
	            // Uncomment this if you want the application to exit after several empty polls
	            /*
	            if (emptyPolls >= maxEmptyPolls) {
	                logger.info("Maximum number of empty polls reached. Exiting processing loop.");
	                keepProcessing = false;
	            }
	            */

	            // Sleep briefly before polling again to avoid tight loop when no messages
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	                logger.warn("Sleep interrupted. Exiting processing loop.");
	                Thread.currentThread().interrupt();
	                keepProcessing = false;
	            }
	        }

	        // Check for thread interruption which would indicate shutdown request
	        if (Thread.currentThread().isInterrupted()) {
	            logger.info("Thread interrupted. Exiting processing loop.");
	            keepProcessing = false;
	        }
	    }

	    logger.info("Exited message processing loop");
	}

	/**
	 * Processes a bulk request to OpenSearch and commits offsets.
	 *
	 * @param client the OpenSearch client
	 * @param consumer the Kafka consumer
	 * @param bulkRequest the bulk request to process
	 */
	private static void processBulkRequest(
	    RestHighLevelClient client,
	    KafkaConsumer<String, String> consumer,
	    BulkRequest bulkRequest) {

	    if (bulkRequest.numberOfActions() > 0) {
	        logger.info("Processing bulk request with {} actions", bulkRequest.numberOfActions());

	        try {
	            // Execute bulk request
	            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

	            // Handle response
	            if (bulkResponse.hasFailures()) {
	                logger.error("Bulk indexing partially failed: {}", bulkResponse.buildFailureMessage());
	            } else {
	                logger.info("Bulk indexing successful. Indexed {} documents in {}ms.",
	                    bulkResponse.getItems().length,
	                    bulkResponse.getTook().getMillis());
	            }

	            // Sleep briefly to avoid overwhelming OpenSearch
	            Thread.sleep(1000);

	            // Commit offsets after successful processing
	            consumer.commitSync();
	            logger.info("Kafka offsets committed successfully");

	        } catch (IOException e) {
	            logger.error("IO error during bulk indexing: {}", e.getMessage());
	        } catch (InterruptedException e) {
	            logger.warn("Sleep interrupted: {}", e.getMessage());
	            Thread.currentThread().interrupt(); // Restore interrupted status
	        } finally {
	            // Reset the bulk request regardless of success/failure
	            bulkRequest.requests().clear();
	        }
	    }
	}

	/**
	 * Extracts the unique ID from a JSON record received from Kafka.
	 *
	 * This method parses the JSON record and extracts the "id" field from the "meta" object.
	 * It includes error handling to manage cases where the JSON structure might not be as expected.
	 *
	 * @param value the JSON string from the Kafka record
	 * @return the extracted ID or a generated fallback ID if extraction fails
	 */
	private static String extractIdFromRecord(String value) {
		try {
			// Parse the JSON and extract the ID from the meta object
			return com.google.gson.JsonParser.parseString(value)
				.getAsJsonObject()
				.get("meta")
				.getAsJsonObject()
				.get("id")
				.getAsString();
		} catch (Exception e) {
			// Handle case where JSON doesn't have the expected structure
			logger.warn("Failed to extract ID from record: {}. Error: {}",
				value.length() > 100 ? value.substring(0, 100) + "..." : value,
				e.getMessage());

			// Fallback: generate a unique ID based on the content hash
			return "fallback_" + Math.abs(value.hashCode());
		}
	}

	/**
	 * Checks if the specified index exists in OpenSearch.
	 *
	 * This method sends a HEAD request to check if the index exists and handles any
	 * exceptions that might occur during the check.
	 *
	 * @param client the RestHighLevelClient to use for the request
	 * @param indexExistsRequest the Request object configured for the existence check
	 * @return true if the index exists, false otherwise
	 */
	private static boolean checkIfIndexExists(RestHighLevelClient client, Request indexExistsRequest) {
		logger.info("Checking if index {} exists", indexExistsRequest.getEndpoint());

		try {
			Response response = client.getLowLevelClient().performRequest(indexExistsRequest);
			boolean indexExists = response.getStatusLine().getStatusCode() == 200;
			logger.info("Index {} check result: {}",
				indexExistsRequest.getEndpoint(),
				indexExists ? "exists" : "does not exist");
			return indexExists;
		} catch (IOException e) {
			// A 404 response will throw an exception, which means the index does not exist
			logger.info("Index {} does not exist. Will create it.", indexExistsRequest.getEndpoint());
			return false;
		}
	}

	/**
	 * Creates and configures a Kafka consumer for reading from Wikimedia topics.
	 *
	 * This method sets up a Kafka consumer with appropriate configurations,
	 * including manual offset commit control and specific deserialization settings.
	 *
	 * @return a configured KafkaConsumer instance
	 */
	private static KafkaConsumer<String, String> createKafkaConsumer() {
		logger.info("Creating Kafka consumer");

		String bootstrapServers = "localhost:9092";
		String groupId = "consumer_open_search";

		logger.info("Consumer configuration: bootstrap.servers={}, group.id={}", bootstrapServers, groupId);

		Properties properties = new Properties();

		// Connection properties
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

		// Deserialization properties
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		// Consumer group properties
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		// Disable auto-commit to ensure manual control of offset commits
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		logger.info("Kafka consumer created successfully");
		return new KafkaConsumer<>(properties);
	}
}
