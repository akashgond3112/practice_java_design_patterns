package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
	private static final String ORDER_PLACED = "Order placed with amount: ${}";

	public static void main(String[] args) {
		// Create payment processor with initial strategy
		PaymentProcessor paymentProcessor = new PaymentProcessor(new CreditCardStrategy());

		// Process payments using different strategies
		log.info(ORDER_PLACED, 100.0);
		paymentProcessor.processPayment(100.0);

		log.info(ORDER_PLACED, 200.0);
		paymentProcessor.setPaymentStrategy(new PayPalStrategy());
		paymentProcessor.processPayment(200.0);

		log.info(ORDER_PLACED, 300.0);
		paymentProcessor.setPaymentStrategy(new CryptoCurrencyStrategy());
		paymentProcessor.processPayment(300.0);

		log.info(ORDER_PLACED, 400.0);
		paymentProcessor.setPaymentStrategy(amount ->
				log.info("Processing bank transfer payment of ${}", amount));
		paymentProcessor.processPayment(400.0);
	}
}