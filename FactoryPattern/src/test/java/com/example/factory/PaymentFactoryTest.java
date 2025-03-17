// PaymentFactoryTest.java
package com.example.factory;

import com.java.design.patterns.BitcoinPayment;
import com.java.design.patterns.CreditCardPayment;
import com.java.design.patterns.PayPalPayment;
import com.java.design.patterns.PaymentFactory;
import com.java.design.patterns.PaymentMethod;
import com.java.design.patterns.PaymentProcessor;
import com.java.design.patterns.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFactoryTest {

	@Test
	void testCreditCardPaymentCreation() {
		PaymentMethod payment = PaymentFactory.createPaymentMethod(PaymentType.CREDIT_CARD);
		assertNotNull(payment);
		assertInstanceOf(CreditCardPayment.class, payment);
		assertEquals("Credit Card", payment.getPaymentType());
	}

	@Test
	void testPayPalPaymentCreation() {
		PaymentMethod payment = PaymentFactory.createPaymentMethod(PaymentType.PAYPAL);
		assertNotNull(payment);
		assertInstanceOf(PayPalPayment.class, payment);
		assertEquals("PayPal", payment.getPaymentType());
	}

	@Test
	void testBitcoinPaymentCreation() {
		PaymentMethod payment = PaymentFactory.createPaymentMethod(PaymentType.BITCOIN);
		assertNotNull(payment);
		assertInstanceOf(BitcoinPayment.class, payment);
		assertEquals("Bitcoin", payment.getPaymentType());
	}

	@Test
	void testPaymentProcessor() {
		PaymentProcessor processor = new PaymentProcessor(PaymentType.CREDIT_CARD);
		assertEquals("Credit Card", processor.getPaymentType());

		PaymentProcessor processor2 = new PaymentProcessor(PaymentType.PAYPAL);
		assertEquals("PayPal", processor2.getPaymentType());

		PaymentProcessor processor3 = new PaymentProcessor(PaymentType.BITCOIN);
		assertEquals("Bitcoin", processor3.getPaymentType());
	}
}