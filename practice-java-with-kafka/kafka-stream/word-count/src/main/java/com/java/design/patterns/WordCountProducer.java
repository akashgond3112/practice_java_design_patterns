package com.java.design.patterns;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Scanner;

public class WordCountProducer {
    private static final Logger logger = LoggerFactory.getLogger(WordCountProducer.class);
    private static final String TOPIC = "word-count-input";

    public static void main(String[] args) {
        logger.info("Starting Word Count Producer");

        // Set up producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
             Scanner scanner = new Scanner(System.in)) {
            
            logger.info("Producer ready to send messages to topic: {}", TOPIC);
            logger.info("Enter text lines to send (type 'exit' to quit):");
            
            String line;
            while (!(line = scanner.nextLine()).equalsIgnoreCase("exit")) {
                // Create and send a producer record
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC, line);
                producer.send(producerRecord, (metadata, exception) -> {
                    if (exception == null) {
                        logger.info("Message sent successfully to topic: {}, partition: {}, offset: {}", 
                                metadata.topic(), metadata.partition(), metadata.offset());
                    } else {
                        logger.error("Error sending message", exception);
                    }
                });
                
                // No need to flush after each message as we're using callbacks
            }
            
            // Final flush before closing
            producer.flush();
            logger.info("Producer finished sending messages");
        } catch (Exception e) {
            logger.error("Error in producer", e);
        }
    }
}
