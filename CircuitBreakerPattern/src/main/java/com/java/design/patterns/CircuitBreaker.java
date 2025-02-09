package com.java.design.patterns;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * CircuitBreaker class implements the Circuit Breaker design pattern. It is used to prevent a system from repeatedly
 * trying to execute an operation that is likely to fail.
 */
@Slf4j
public class CircuitBreaker {

	private final PaymentGatewayService service;
	private final long timeout;
	private final int failureThreshold;
	private final long retryTimeout;

	private int failureCount;
	private long lastFailureTime;
	@Getter
	private State state;

	/**
	 * Enum representing the state of the Circuit Breaker.
	 */
	public enum State {
		CLOSED, OPEN, HALF_OPEN
	}

	/**
	 * Constructor to initialize the CircuitBreaker.
	 *
	 * @param service
	 * 		the payment gateway service to be used
	 * @param timeout
	 * 		the timeout duration for the service call
	 * @param failureThreshold
	 * 		the number of failures before opening the circuit
	 * @param retryTimeout
	 * 		the duration to wait before retrying after a failure
	 */
	public CircuitBreaker(PaymentGatewayService service, long timeout, int failureThreshold, long retryTimeout) {
		this.service = service;
		this.timeout = timeout;
		this.failureThreshold = failureThreshold;
		this.retryTimeout = retryTimeout;
		this.state = State.CLOSED;
		this.failureCount = 0;
	}

	/**
	 * Processes a payment through the payment gateway service.
	 *
	 * @param amount
	 * 		the amount to be processed
	 * @return the result of the payment processing
	 * @throws Exception
	 * 		if the circuit breaker is open or the payment processing fails
	 */
	public String processPayment(double amount) throws Exception {
		if (state == State.OPEN) {
			if (System.currentTimeMillis() - lastFailureTime >= retryTimeout) {
				state = State.HALF_OPEN;
				log.info("Circuit Breaker state changed to HALF_OPEN");
			} else {
				throw new Exception("Circuit Breaker is OPEN");
			}
		}

		try {
			String result = service.processPayment(amount);
			if (state == State.HALF_OPEN) {
				state = State.CLOSED;
				failureCount = 0;
				log.info("Circuit Breaker state changed to CLOSED");
			}
			return result;
		} catch (Exception e) {
			handleFailure();
			throw e;
		}
	}

	/**
	 * Handles a failure in the payment processing. Increments the failure count and changes the state to OPEN if the
	 * failure threshold is reached.
	 */
	private void handleFailure() {
		failureCount++;
		if (failureCount >= failureThreshold) {
			state = State.OPEN;
			lastFailureTime = System.currentTimeMillis();
			log.info("Circuit Breaker state changed to OPEN");
		}
	}

}