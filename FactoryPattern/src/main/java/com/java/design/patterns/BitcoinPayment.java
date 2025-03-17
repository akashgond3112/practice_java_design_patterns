package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BitcoinPayment implements PaymentMethod {
	@Override
	public void processPayment(double amount) {
		// Implementation for processing Bitcoin payment
		log.info("Processing Bitcoin payment of {}", amount);
	}

	@Override
	public String getPaymentType() {
		return "Bitcoin";
	}
}
