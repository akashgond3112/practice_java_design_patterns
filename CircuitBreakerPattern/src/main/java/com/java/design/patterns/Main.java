package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

	public static void main(String[] args) {
		// Initialize payment processor with 2-second delay and 50% failure rate
		PaymentGatewayService paymentProcessor = new PaymentProcessor(2000, 0.5);

		// Configure circuit breaker with 3 second timeout, 2 failures threshold, and 5 second retry timeout
		CircuitBreaker circuitBreaker = new CircuitBreaker(paymentProcessor, 3000, 2, 5000);

		// Simulate payment processing
		for (int i = 0; i < 5; i++) {
			try {
				String result = circuitBreaker.processPayment(100.00);
				log.info("Attempt {}: {}", i + 1, result);
			} catch (Exception e) {
				log.error("Attempt {}: {}", i + 1, e.getMessage());
			}

			log.info("Current Circuit Breaker State: {}", circuitBreaker.getState());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
