package com.java.design.patterns;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class CreateWordCountTopics {
    private static final Logger logger = LoggerFactory.getLogger(CreateWordCountTopics.class);
    private static final String INPUT_TOPIC = "word-count-input";
    private static final String OUTPUT_TOPIC = "word-count-output";
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    public static void main(String[] args) {
        logger.info("Starting topic creation for word count application");

        // Set up admin client properties
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(properties)) {
            // Get existing topics
            Set<String> existingTopics = adminClient.listTopics().names().get();
            logger.info("Existing topics: {}", existingTopics);

            // Create topics if they don't exist
            List<NewTopic> topicsToCreate = Arrays.asList(
                    createTopicIfNotExists(existingTopics, INPUT_TOPIC, 1, (short) 1),
                    createTopicIfNotExists(existingTopics, OUTPUT_TOPIC, 1, (short) 1)
            ).stream()
                    .filter(Objects::nonNull)
                    .toList();

            if (!topicsToCreate.isEmpty()) {
                CreateTopicsResult result = adminClient.createTopics(topicsToCreate);
                
                // Wait for completion and log results
                for (NewTopic topic : topicsToCreate) {
                    waitForTopicCreation(result, topic);
                }
            } else {
                logger.info("All required topics already exist");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted during topic creation", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error creating topics", e);
        }
    }
    
    private static void waitForTopicCreation(CreateTopicsResult result, NewTopic topic) throws InterruptedException {
        KafkaFuture<Void> future = result.values().get(topic.name());
        try {
            future.get();
            logger.info("Topic {} created successfully", topic.name());
        } catch (InterruptedException e) {
            logger.error("Interrupted while creating topic {}", topic.name());
            throw e;
        } catch (ExecutionException e) {
            logger.error("Failed to create topic {}: {}", topic.name(), e.getMessage());
        }
    }

    private static NewTopic createTopicIfNotExists(Set<String> existingTopics, String topicName, int numPartitions, short replicationFactor) {
        if (!existingTopics.contains(topicName)) {
            logger.info("Topic {} doesn't exist, creating it", topicName);
            return new NewTopic(topicName, numPartitions, replicationFactor);
        } else {
            logger.info("Topic {} already exists", topicName);
            return null;
        }
    }
}
