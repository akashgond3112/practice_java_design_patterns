package com.java.design.patterns;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class WordCountConsumer {
    private static final Logger logger = LoggerFactory.getLogger(WordCountConsumer.class);
    private static final String TOPIC = "word-count-output";
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
    
    public static void main(String[] args) {
        logger.info("Starting Word Count Consumer");
        
        // Set up consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "word-count-consumer-group");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // Create the consumer
        final CountDownLatch latch = new CountDownLatch(1);
        final WordCountConsumerRunner consumerRunner = new WordCountConsumerRunner(properties, latch);
        
        // Start the consumer thread
        final Thread consumerThread = new Thread(consumerRunner);
        consumerThread.start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook");
            consumerRunner.shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("Application got interrupted", e);
                Thread.currentThread().interrupt();
            }
            logger.info("Application has exited");
        }));
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            logger.info("Application is closing");
        }
    }
    
    private static class WordCountConsumerRunner implements Runnable {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final KafkaConsumer<String, Long> consumer;
        private final CountDownLatch latch;
        private final Map<String, Long> currentWordCounts = new HashMap<>();
        private long lastDisplayTime = 0;
        
        public WordCountConsumerRunner(Properties properties, CountDownLatch latch) {
            this.consumer = new KafkaConsumer<>(properties);
            this.latch = latch;
        }
        
        @Override
        public void run() {
            try {
                consumer.subscribe(Collections.singleton(TOPIC));
                logger.info("Consumer subscribed to topic: {}", TOPIC);
                logger.info("Waiting for messages... (Press Ctrl+C to exit)");
                
                while (!closed.get()) {
                    ConsumerRecords<String, Long> records = consumer.poll(Duration.ofMillis(100));
                    
                    for (ConsumerRecord<String, Long> consumerRecord : records) {
                        String word = consumerRecord.key();
                        Long count = consumerRecord.value();
                        
                        // Update word count map
                        currentWordCounts.put(word, count);
                    }
                    
                    // Display word counts periodically or when there are new records
                    if (!records.isEmpty() || System.currentTimeMillis() - lastDisplayTime > 5000) {
                        displayWordCounts();
                        lastDisplayTime = System.currentTimeMillis();
                    }
                }
            } catch (WakeupException e) {
                // Ignore if closing
                if (!closed.get()) throw e;
            } finally {
                consumer.close();
                latch.countDown();
            }
        }
        
        private void displayWordCounts() {
            if (currentWordCounts.isEmpty()) {
                logger.info("No word counts received yet");
                return;
            }
            
            // Use logger instead of System.out
            logger.info("\n===== CURRENT WORD COUNTS =====");
            logger.info("Word                 | Count");
            logger.info("----------------------|-------");
            
            // Sort by count in descending order
            StringBuilder tableBuilder = new StringBuilder();
            currentWordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(entry -> {
                        String word = entry.getKey();
                        Long count = entry.getValue();
                        tableBuilder.append(String.format("%-20s | %d%n", word, count));
                    });
            
            if (logger.isInfoEnabled()) {
                logger.info(tableBuilder.toString());
            }
            logger.info("==============================\n");
        }
        
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }
    }
}
