package com.java.design.patterns;


import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class WikiMediaChangeHandler implements EventHandler {

	private final KafkaProducer<String, String> producer;
	private final String topic;

	public WikiMediaChangeHandler(KafkaProducer<String, String> producer, String topic) {
		this.producer = producer;
		this.topic = topic;
	}

	@Override
	public void onOpen() {
		// Handle open event
	}

	@Override
	public void onClosed() {
		// Handle closed event
	}

	@Override
	public void onMessage(String event, MessageEvent messageEvent) {
		// Handle incoming messages and produce to Kafka
		producer.send(new ProducerRecord<>(topic, event, messageEvent.getData()));
	}

	@Override
	public void onComment(String comment) {
		// Handle comments if necessary
	}

	@Override
	public void onError(Throwable t) {
		// Handle errors
		t.printStackTrace();
	}
}
