package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CryptoCurrencyStrategy implements PaymentStrategy {
	@Override
	public void processPayment(double amount) {
		log.info("Processing cryptocurrency payment of ${}", amount);
		// Add cryptocurrency processing logic here
	}
}