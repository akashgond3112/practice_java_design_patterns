package com.java.design.patterns;

public class PaymentProcessor {

	private final PaymentMethod paymentMethod;

	public PaymentProcessor(PaymentType paymentType) {
		this.paymentMethod = PaymentFactory.createPaymentMethod(paymentType);
	}

	public void processPayment(double amount) {
		paymentMethod.processPayment(amount);
	}

	public String getPaymentType() {
		return paymentMethod.getPaymentType();
	}
}
