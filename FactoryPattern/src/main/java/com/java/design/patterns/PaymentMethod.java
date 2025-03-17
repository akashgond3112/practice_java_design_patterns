package com.java.design.patterns;

public interface PaymentMethod {
	void processPayment(double amount);
	String getPaymentType();
}
