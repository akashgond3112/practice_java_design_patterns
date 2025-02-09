package com.java.design.patterns;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentProcessor {
	@Setter
	private PaymentStrategy paymentStrategy;

	public PaymentProcessor(PaymentStrategy paymentStrategy) {
		this.paymentStrategy = paymentStrategy;
	}

	public void processPayment(double amount) {
		paymentStrategy.processPayment(amount);
	}
}
