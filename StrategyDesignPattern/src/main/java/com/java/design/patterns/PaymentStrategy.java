package com.java.design.patterns;

@FunctionalInterface
public interface PaymentStrategy {
	void processPayment(double amount);
}
