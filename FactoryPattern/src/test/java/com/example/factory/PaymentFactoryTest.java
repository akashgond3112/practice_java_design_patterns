// PaymentFactoryTest.java
package com.example.factory;

import com.java.design.patterns.BitcoinPayment;
import com.java.design.patterns.CreditCardPayment;
import com.java.design.patterns.PayPalPayment;
import com.java.design.patterns.PaymentFactory;
import com.java.design.patterns.PaymentMethod;
import com.java.design.patterns.PaymentProcessor;
import com.java.design.patterns.PaymentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PaymentFactoryTest {

    @Test
    void testCreditCardPaymentCreation() {
        log.info("Testing credit card payment creation");
        PaymentMethod payment = PaymentFactory.createPaymentMethod(PaymentType.CREDIT_CARD);
        assertNotNull(payment);
        assertInstanceOf(CreditCardPayment.class, payment);
        assertEquals("Credit Card", payment.getPaymentType());
        log.info("Credit card payment test completed successfully");
    }

    @Test
    void testPayPalPaymentCreation() {
        log.info("Testing PayPal payment creation");
        PaymentMethod payment = PaymentFactory.createPaymentMethod(PaymentType.PAYPAL);
        assertNotNull(payment);
        assertInstanceOf(PayPalPayment.class, payment);
        assertEquals("PayPal", payment.getPaymentType());
        log.info("PayPal payment test completed successfully");
    }

    @Test
    void testBitcoinPaymentCreation() {
        log.info("Testing Bitcoin payment creation");
        PaymentMethod payment = PaymentFactory.createPaymentMethod(PaymentType.BITCOIN);
        assertNotNull(payment);
        assertInstanceOf(BitcoinPayment.class, payment);
        assertEquals("Bitcoin", payment.getPaymentType());
        log.info("Bitcoin payment test completed successfully");
    }

    @Test
    void testPaymentProcessor() {
        log.info("Testing payment processor with different payment types");
        PaymentProcessor processor = new PaymentProcessor(PaymentType.CREDIT_CARD);
        assertEquals("Credit Card", processor.getPaymentType());
        log.debug("Credit card processor verified");

        PaymentProcessor processor2 = new PaymentProcessor(PaymentType.PAYPAL);
        assertEquals("PayPal", processor2.getPaymentType());
        log.debug("PayPal processor verified");

        PaymentProcessor processor3 = new PaymentProcessor(PaymentType.BITCOIN);
        assertEquals("Bitcoin", processor3.getPaymentType());
        log.debug("Bitcoin processor verified");
        
        log.info("Payment processor tests completed successfully");
    }
}