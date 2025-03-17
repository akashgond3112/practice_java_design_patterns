package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayPalPayment  implements PaymentMethod{
	@Override
	public void processPayment(double amount) {
		// Implementation for processing PayPal payment
		log.info("Processing PayPal payment of {}", amount);
	}

	@Override
	public String getPaymentType() {
		return "PayPal";
	}
}
