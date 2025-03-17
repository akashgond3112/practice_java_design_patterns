package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CreditCardPayment implements PaymentMethod {
	@Override
	public void processPayment(double amount) {
		// Implementation for processing credit card payment
		log.info("Processing credit card payment of {}", amount);
	}

	@Override
	public String getPaymentType() {
		return "Credit Card";
	}
}
