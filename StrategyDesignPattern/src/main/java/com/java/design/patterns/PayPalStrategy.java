package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayPalStrategy implements PaymentStrategy {
	@Override
	public void processPayment(double amount) {
		log.info("Processing PayPal payment of ${}", amount);
		// Add PayPal processing logic here
	}
}