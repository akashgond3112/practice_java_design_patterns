package com.java.design.patterns;

public interface PaymentGatewayService {
	String processPayment(double amount) throws Exception;
}
