package com.java.design.patterns;

import java.util.Random;

public class PaymentProcessor implements PaymentGatewayService {
	private final long processingTime;
	private final double failureRate;
	private final Random random = new Random();

	public PaymentProcessor(long processingTime, double failureRate) {
		this.processingTime = processingTime;
		this.failureRate = failureRate;
	}

	@Override
	public String processPayment(double amount) throws Exception {
		// Simulate processing delay
		Thread.sleep(processingTime);

		// Simulate random failures
		if (random.nextDouble() < failureRate) {
			throw new Exception("Payment processing failed");
		}

		return "Payment processed successfully: $" + amount;
	}
}
