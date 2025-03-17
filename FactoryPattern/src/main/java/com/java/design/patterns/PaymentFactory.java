package com.java.design.patterns;

public class PaymentFactory {

	private PaymentFactory() {
	}

	public static PaymentMethod createPaymentMethod(PaymentType type) {
		return switch (type) {
			case CREDIT_CARD -> new CreditCardPayment();
			case PAYPAL -> new PayPalPayment();
			case BITCOIN -> new BitcoinPayment();
		};
	}
}
