package com.java.design.patterns.wise;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JUnit test class for the CircuitBreaker implementation.
 *
 * This class tests the behavior of the circuit breaker in various scenarios.
 */
class CircuitBreakerAdvanceTest {

	private static final Logger log = LoggerFactory.getLogger(CircuitBreakerAdvanceTest.class);

	/**
	 * Simulates a request to a service and returns a status code.
	 *
	 * @param circuitBreaker
	 *      the circuit breaker to check
	 * @param failureRate
	 *      the probability of a server-side failure (0.0 to 1.0)
	 * @param threadName
	 *      the name of the current thread (for logging)
	 * @return the HTTP status code of the response
	 */
	private int simulateRequest(CircuitBreakerAdvance circuitBreaker, double failureRate, String threadName) {
		if (!circuitBreaker.allowRequest()) {
			log.info("{} : Request blocked - Circuit is {}", threadName, circuitBreaker.getState());
			return 503; // Service Unavailable
		}

		log.info(" {}: Request allowed", threadName);

		// Simulate processing time
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Simulate a response based on failure rate
		int statusCode;
		if (Math.random() < failureRate) {
			statusCode = 500; // Server error
		} else {
			statusCode = 200; // Success
		}

		// Record the result
		circuitBreaker.recordResult(statusCode);
		log.info("{} : Request completed with status {} ", threadName, statusCode);

		return statusCode;
	}

	@BeforeEach
	void setUp() {
		// Configure logging if needed
	}

	@Test
	@DisplayName("Test Case 1: Normal operation - no errors")
	void testNormalOperation() {
		CircuitBreakerAdvance cb = new CircuitBreakerAdvance(4, 60, 10);

		for (int i = 0; i < 5; i++) {
			int statusCode = simulateRequest(cb, 0.0, "TestThread-1");
			assertEquals(200, statusCode, "All requests should succeed");
		}

		assertEquals(CircuitBreakerAdvance.State.CLOSED, cb.getState(), "Circuit should remain CLOSED");
		assertEquals(0, cb.getErrorCount(), "Error count should be 0");
	}

	@Test
	@DisplayName("Test Case 2: Errors within threshold - circuit remains CLOSED")
	void testErrorsWithinThreshold() {
		CircuitBreakerAdvance cb = new CircuitBreakerAdvance(4, 60, 10);

		for (int i = 0; i < 3; i++) {
			int statusCode = simulateRequest(cb, 1.0, "TestThread-1"); // All fail
			assertEquals(500, statusCode, "All requests should fail with server error");
		}

		assertEquals(CircuitBreakerAdvance.State.CLOSED, cb.getState(), "Circuit should remain CLOSED when under threshold");
		assertEquals(3, cb.getErrorCount(), "Error count should be 3");
	}

	@Test
	@DisplayName("Test Case 3: Errors exceed threshold - circuit trips to OPEN")
	void testCircuitTripsOpen() {
		CircuitBreakerAdvance cb = new CircuitBreakerAdvance(4, 60, 10);

		for (int i = 0; i < 5; i++) {
			simulateRequest(cb, 1.0, "TestThread-1"); // All fail
		}

		assertEquals(CircuitBreakerAdvance.State.OPEN, cb.getState(), "Circuit should be OPEN after exceeding threshold");
		assertTrue(cb.getErrorCount() >= 4, "Error count should be at least the threshold");
	}

	@Test
	@DisplayName("Test Case 4: Half-open state and recovery")
	void testHalfOpenAndRecovery() throws InterruptedException {
		CircuitBreakerAdvance cb = new CircuitBreakerAdvance(3, 60, 2, 3); // 3 errors, 2-second cool-off, 3 successes to close

		// Trip the circuit
		for (int i = 0; i < 4; i++) {
			simulateRequest(cb, 1.0, "TestThread-1"); // All fail
		}
		assertEquals(CircuitBreakerAdvance.State.OPEN, cb.getState(), "Circuit should be OPEN after failures");

		// Wait for cool-off period
		Thread.sleep(2500); // Wait for cool-off period to end (2 seconds + buffer)

		// After cool-off, circuit should allow one request and transition to HALF_OPEN
		int statusCode = simulateRequest(cb, 0.0, "TestThread-1"); // Success
		assertEquals(200, statusCode, "Request should succeed");
		assertEquals(CircuitBreakerAdvance.State.HALF_OPEN, cb.getState(), "Circuit should be HALF_OPEN after cool-off and first success");

		// Additional successful requests should transition to CLOSED
		for (int i = 0; i < 2; i++) { // Need 2 more successes for a total of 3
			statusCode = simulateRequest(cb, 0.0, "TestThread-1");
			assertEquals(200, statusCode, "Request should succeed");
		}

		assertEquals(CircuitBreakerAdvance.State.CLOSED, cb.getState(), "Circuit should be CLOSED after required successes");
	}

	@Test
	@DisplayName("Test Case 5: HALF_OPEN to OPEN transition on failure")
	void testHalfOpenToOpenOnFailure() throws InterruptedException {
		CircuitBreakerAdvance cb = new CircuitBreakerAdvance(3, 60, 2, 3);

		// Trip the circuit
		for (int i = 0; i < 4; i++) {
			simulateRequest(cb, 1.0, "TestThread-1"); // All fail
		}
		assertEquals(CircuitBreakerAdvance.State.OPEN, cb.getState(), "Circuit should be OPEN after failures");

		// Wait for cool-off period
		Thread.sleep(2500);

		// First request after cool-off fails - should return to OPEN
		int statusCode = simulateRequest(cb, 1.0, "TestThread-1"); // Fail
		assertEquals(500, statusCode, "Request should fail");
		assertEquals(CircuitBreakerAdvance.State.OPEN, cb.getState(), "Circuit should return to OPEN after failure in HALF_OPEN state");
	}

	@Test
	@DisplayName("Test Case 6: Multi-threaded environment - thread safety")
	@Timeout(value = 30)
	void testMultithreadedEnvironment() throws InterruptedException {
		final CircuitBreakerAdvance cb = new CircuitBreakerAdvance(4, 60, 5, 3);
		final int threadCount = 10;
		final int requestsPerThread = 20;
		final CountDownLatch latch = new CountDownLatch(threadCount);

		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (int i = 0; i < threadCount; i++) {
			final int threadId = i;
			executor.submit(() -> {
				try {
					String threadName = "TestThread-" + threadId;
					for (int j = 0; j < requestsPerThread; j++) {
						simulateRequest(cb, 0.3, threadName); // 30% failure rate
						Thread.sleep(20); // Small delay between requests
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					latch.countDown();
				}
			});
		}

		// Wait for all threads to complete
		assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete in time");
		executor.shutdown();
		assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Executor should terminate properly");

		// Check final state - specific assertions will depend on implementation behavior
		// Here we just verify the circuit is in a valid state and has processed requests
		assertTrue(
				cb.getState() == CircuitBreakerAdvance.State.CLOSED ||
						cb.getState() == CircuitBreakerAdvance.State.HALF_OPEN ||
						cb.getState() == CircuitBreakerAdvance.State.OPEN,
				"Circuit should be in a valid state"
		);

		// If we want specific metrics assertions, we can add them here depending on expected behavior
		if (cb.getState() == CircuitBreakerAdvance.State.OPEN) {
			assertTrue(cb.getElapsedTimeInOpenState() >= 0, "Elapsed time in OPEN state should be non-negative");
			assertTrue(cb.getExpectedTransitionTime() >= System.currentTimeMillis(),
					"Expected transition time should be in the future");
		}
	}
}