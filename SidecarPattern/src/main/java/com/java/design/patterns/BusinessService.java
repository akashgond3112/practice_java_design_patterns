package com.java.design.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BusinessService {
	private static final Logger logger = LoggerFactory.getLogger(BusinessService.class);
	private final Random random = new Random();

	public void processRequest() {
		logger.info("Processing business request");

		// Simulate business logic
		try {
			int processingTime = random.nextInt(500) + 100;
			Thread.sleep(processingTime);

			// Occasionally simulate errors
			if (random.nextDouble() < 0.1) {
				throw new RuntimeException("Simulated business logic error");
			}

			logger.info("Request processed successfully in {}ms", processingTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Processing interrupted", e);
		}
	}
}
