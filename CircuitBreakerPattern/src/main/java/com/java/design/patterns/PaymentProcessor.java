package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
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
		log.info("Processing payment of ${}", amount);
		Thread.sleep(processingTime);

		if (random.nextDouble() < failureRate) {
			log.error("Payment processing failed for amount ${}", amount);
			throw new Exception("Payment processing failed");
		}

		log.info("Payment processed successfully for amount ${}", amount);
		return "Payment processed successfully: $" + amount;
	}
}
