package com.java.design.patterns;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

	public enum State {
		CLOSED, OPEN, HALF_OPEN
	}

	public CircuitBreaker(PaymentGatewayService service, long timeout, int failureThreshold, long retryTimeout) {
		this.service = service;
		this.timeout = timeout;
		this.failureThreshold = failureThreshold;
		this.retryTimeout = retryTimeout;
		this.state = State.CLOSED;
		this.failureCount = 0;
	}

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

	private void handleFailure() {
		failureCount++;
		if (failureCount >= failureThreshold) {
			state = State.OPEN;
			lastFailureTime = System.currentTimeMillis();
			log.info("Circuit Breaker state changed to OPEN");
		}
	}

}
