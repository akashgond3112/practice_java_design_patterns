package com.java.design.patterns.wise;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A thread-safe implementation of the Circuit Breaker pattern.
 *
 * The Circuit Breaker prevents system overload by monitoring failures and blocking requests when failures exceed a
 * threshold. After a cool-off period, it allows traffic again.
 *
 * This implementation uses a sliding window to track errors and supports three states: CLOSED (normal operation), OPEN
 * (blocking requests), and HALF_OPEN (testing if service has recovered).
 */
@Slf4j
public class CircuitBreakerAdvance {

	/**
	 * Possible states of the circuit breaker
	 */
	public enum State {
		CLOSED,    // Normal operation - all requests allowed
		HALF_OPEN, // Testing if service has recovered - limited requests allowed
		OPEN       // Service is down - all requests blocked
	}

	private final int errorThreshold;
	private final long timeWindowMillis;
	private final long coolOffMillis;
	private final Queue<Long> errorTimestamps = new ConcurrentLinkedQueue<>();
	private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
	private final AtomicLong openedAt = new AtomicLong(0);
	private final AtomicLong lastRequestTime = new AtomicLong(0);
	private final AtomicInteger consecutiveSuccesses = new AtomicInteger(0);
	private final int requiredSuccessesToClose;

	/**
	 * Creates a new CircuitBreaker instance.
	 *
	 * @param errorThreshold
	 * 		Number of errors required to trip the circuit
	 * @param timeWindowSeconds
	 * 		Duration of the sliding window for error tracking (in seconds)
	 * @param coolOffSeconds
	 * 		Duration of cool-off period after circuit trips (in seconds)
	 * @param requiredSuccessesToClose
	 * 		Number of consecutive successful requests required to transition from HALF_OPEN to CLOSED
	 */
	public CircuitBreakerAdvance(int errorThreshold, long timeWindowSeconds, long coolOffSeconds,
			int requiredSuccessesToClose) {
		this.errorThreshold = errorThreshold;
		this.timeWindowMillis = timeWindowSeconds * 1000;
		this.coolOffMillis = coolOffSeconds * 1000;
		this.requiredSuccessesToClose = requiredSuccessesToClose;
		log.info("CircuitBreaker initialized with threshold={}, timeWindow={}, coolOff={}", errorThreshold,
				timeWindowSeconds, coolOffSeconds);
	}

	/**
	 * Creates a new CircuitBreaker instance with default value for success threshold.
	 *
	 * @param errorThreshold
	 * 		Number of errors required to trip the circuit
	 * @param timeWindowSeconds
	 * 		Duration of the sliding window for error tracking (in seconds)
	 * @param coolOffSeconds
	 * 		Duration of cool-off period after circuit trips (in seconds)
	 */
	public CircuitBreakerAdvance(int errorThreshold, long timeWindowSeconds, long coolOffSeconds) {
		this(errorThreshold, timeWindowSeconds, coolOffSeconds, 2);
	}

	/**
	 * Determines if a request should be allowed based on the circuit state.
	 *
	 * @return true if the request is allowed, false otherwise
	 */
	public boolean allowRequest() {
		lastRequestTime.set(System.currentTimeMillis());
		State currentState = state.get();

		switch (currentState) {
		case CLOSED:
			evictOldErrors();
			if (errorTimestamps.size() >= errorThreshold) {
				// Attempt to trip the circuit to OPEN
				boolean tripped = state.compareAndSet(State.CLOSED, State.OPEN);
				if (tripped) {
					openedAt.set(System.currentTimeMillis());
					log.warn("Circuit TRIPPED: too many errors detected");
					resetConsecutiveSuccesses();
				}
				return false;
			}
			return true;

		case HALF_OPEN:
			// In HALF_OPEN state, we allow only limited requests to test
			// Use consecutive request counter to allow only one request at a time
			return true;

		case OPEN:
			long now = System.currentTimeMillis();
			if (now - openedAt.get() >= coolOffMillis) {
				// Attempt to transition to HALF_OPEN
				boolean transitioned = state.compareAndSet(State.OPEN, State.HALF_OPEN);
				if (transitioned) {
					log.info("Circuit transitioned from OPEN to HALF_OPEN");
					resetConsecutiveSuccesses();
				}
				return true;
			}
			return false;

		default:
			return false;
		}
	}

	/**
	 * Records a request result based on HTTP status code.
	 *
	 * @param statusCode
	 * 		The HTTP status code of the response
	 */
	public void recordResult(int statusCode) {
		if (statusCode >= 500) {
			recordFailure(statusCode);
		} else {
			recordSuccess();
		}
	}

	/**
	 * Records a server error (status code >= 500).
	 *
	 * @param statusCode
	 * 		The HTTP status code of the error
	 */
	public void recordFailure(int statusCode) {
		if (statusCode >= 500) {
			errorTimestamps.add(System.currentTimeMillis());
			resetConsecutiveSuccesses();
			log.info("Recorded server error with status code: {}", statusCode);

			// If in HALF_OPEN state, immediately trip back to OPEN
			if (state.get() == State.HALF_OPEN && state.compareAndSet(State.HALF_OPEN, State.OPEN)) {
				openedAt.set(System.currentTimeMillis());
				log.warn("Circuit returned to OPEN state due to failure in HALF_OPEN state");
			}

		}
	}

	/**
	 * Records a successful request. Handles the transition from HALF_OPEN to CLOSED after sufficient successful
	 * requests.
	 */
	public void recordSuccess() {
		if (state.get() == State.HALF_OPEN) {
			int successes = consecutiveSuccesses.incrementAndGet();
			log.info("Recorded success in HALF_OPEN state. Consecutive successes: {}", successes);

			if (successes >= requiredSuccessesToClose && state.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
					log.info("Circuit closed after {} consecutive successful requests", successes);
					errorTimestamps.clear();
					resetConsecutiveSuccesses();
				}

		}
	}

	/**
	 * Resets the consecutive success counter.
	 */
	private void resetConsecutiveSuccesses() {
		consecutiveSuccesses.set(0);
	}

	/**
	 * Removes error timestamps that are older than the time window. This maintains the sliding window of errors.
	 */
	private void evictOldErrors() {
		long now = System.currentTimeMillis();
		while (!errorTimestamps.isEmpty()) {
			Long oldest = errorTimestamps.peek();
			if (oldest == null || (now - oldest) <= timeWindowMillis) {
				break;
			}
			errorTimestamps.poll();
		}
	}

	/**
	 * Gets the current state of the circuit breaker.
	 *
	 * @return the current state
	 */
	public State getState() {
		return state.get();
	}

	/**
	 * Gets the current count of tracked errors within the time window.
	 *
	 * @return the error count
	 */
	public int getErrorCount() {
		evictOldErrors();
		return errorTimestamps.size();
	}

	/**
	 * Gets the timestamp when the circuit was last opened (in milliseconds).
	 *
	 * @return the timestamp when the circuit was opened, or 0 if never opened
	 */
	public long getOpenedAt() {
		return openedAt.get();
	}

	/**
	 * Gets the time elapsed since the circuit was opened (in milliseconds).
	 *
	 * @return the time elapsed, or 0 if the circuit was never opened
	 */
	public long getElapsedTimeInOpenState() {
		long openTime = openedAt.get();
		if (openTime == 0) {
			return 0;
		}
		return System.currentTimeMillis() - openTime;
	}

	/**
	 * Gets the time when the circuit is expected to transition from OPEN to HALF_OPEN.
	 *
	 * @return the expected transition time in milliseconds since epoch, or 0 if not in OPEN state
	 */
	public long getExpectedTransitionTime() {
		long openTime = openedAt.get();
		if (openTime == 0 || state.get() != State.OPEN) {
			return 0;
		}
		return openTime + coolOffMillis;
	}

	/**
	 * Resets the circuit breaker to CLOSED state and clears error history. This method should be used with caution,
	 * typically for testing or manual intervention.
	 */
	public void reset() {
		errorTimestamps.clear();
		state.set(State.CLOSED);
		openedAt.set(0);
		resetConsecutiveSuccesses();
		log.info("Circuit breaker manually reset to CLOSED state");
	}

	/**
	 * Checks if the circuit is available for requests.
	 *
	 * @return true if the circuit is CLOSED or HALF_OPEN, false if OPEN
	 */
	public boolean isAvailable() {
		State currentState = state.get();
		return currentState == State.CLOSED || (currentState == State.HALF_OPEN && consecutiveSuccesses.get() < requiredSuccessesToClose);
	}
}
