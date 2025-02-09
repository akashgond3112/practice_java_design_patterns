package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreditCardStrategy implements PaymentStrategy {
	@Override
	public void processPayment(double amount) {
		log.info("Processing credit card payment of ${}", amount);
		// Add credit card processing logic here
	}
}
