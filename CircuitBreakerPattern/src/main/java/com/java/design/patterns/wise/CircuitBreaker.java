package com.java.design.patterns.wise;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <pre>
 * ### **Wise Interview Question: Circuit Breaker Implementation**
 *
 * **Problem Statement:**
 * You are tasked with implementing a **circuit breaker** mechanism for an API that frequently gets overloaded. The goal is to control the behavior of the API requests to prevent server overload. Specifically:
 *
 * 1. **Threshold for Errors:**
 *    - If the server throws errors (HTTP status code >= 500) **more than 4 times in the last 1 minute**, the circuit breaker should activate.
 *    - Once activated, the circuit breaker should **stop forwarding requests** to the server and instead **return an error with a message**.
 *
 * 2. **Optimization:**
 *    - Since the response of requests older than 1 minute is irrelevant, optimize the solution to track only the last 1 minute of errors.
 *
 * 3. **Edge Cases:**
 *    - **Client-Side Errors:** If the error is due to client-side issues (e.g., wrong authentication token), the circuit breaker should **not** activate. It should only activate for server-side errors (status code >= 500).
 *    - **Cool-off Period:** After the circuit breaker is activated, introduce a **cool-off period** (e.g., 10 seconds) during which all requests are blocked. After the cool-off period, the circuit breaker should reset and allow requests again.
 *
 * 4. **Multi-Threaded Environment:**
 *    - Ensure the solution is **thread-safe** and can handle concurrent requests in a multi-threaded environment.
 *
 * 5. **Production Readiness:**
 *    - What additional changes or considerations would you make before deploying this code to production?
 *
 * ---
 *
 * ### **Follow-Up Questions:**
 *
 * 1. **Optimization:**
 *    - How can you optimize the solution to track only the last 1 minute of errors? (Hint: Use a queue to maintain a sliding window of error timestamps.)
 *
 * 2. **Edge Cases:**
 *    - Should the circuit breaker activate for client-side errors (e.g., 4xx status codes)? Why or why not?
 *    - How would you implement a **cool-off period** after the circuit breaker is activated?
 *
 * 3. **Thread Safety:**
 *    - How would you handle this in a **multi-threaded environment**? What mechanisms would you use to ensure thread safety? (Hint: Use locks, atomic variables, or immutable objects.)
 *
 * 4. **Production Readiness:**
 *    - What additional changes or improvements would you make before pushing this code to production? (Hint: Logging, monitoring, configuration management, etc.)
 *
 * ---
 *
 * This is a well-structured problem that tests your understanding of:
 * - **Circuit Breaker Design Pattern**
 * - **Error Handling and Thresholds**
 * - **Optimization Techniques (e.g., sliding window with a queue)**
 * - **Thread Safety in Concurrent Systems**
 * - **Production-Level Code Considerations**
 * </pre>*/
public class CircuitBreaker {

	public enum State {
		CLOSED, OPEN
	}

	private final int errorThreshold;
	private final long timeWindowMillis;
	private final long coolOffMillis;
	private final Queue<Long> errorTimestamps = new ConcurrentLinkedQueue<>();
	private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
	private final AtomicLong openedAt = new AtomicLong(0);

	public CircuitBreaker(int errorThreshold, long timeWindowSeconds, long coolOffSeconds) {
		this.errorThreshold = errorThreshold;
		this.timeWindowMillis = timeWindowSeconds * 1000;
		this.coolOffMillis = coolOffSeconds * 1000;
	}

	public boolean allowRequest() {
		if (state.get() == State.CLOSED) {
			evictOldErrors();
			if (errorTimestamps.size() >= errorThreshold) {
				// Attempt to trip the circuit to OPEN
				boolean tripped = state.compareAndSet(State.CLOSED, State.OPEN);
				if (tripped) {
					openedAt.set(System.currentTimeMillis());
				}
				return false;
			}
			return true;
		} else {
			long now = System.currentTimeMillis();
			if (now - openedAt.get() >= coolOffMillis) {
				// Attempt to transition back to CLOSED
				boolean transitioned = state.compareAndSet(State.OPEN, State.CLOSED);
				if (transitioned) {
					errorTimestamps.clear();
				}
				return true;
			}
			return false;
		}
	}

	public void recordError(int statusCode) {
		if (statusCode >= 500) {
			errorTimestamps.add(System.currentTimeMillis());
		}
	}

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

	// For testing and monitoring purposes
	public State getState() {
		return state.get();
	}

	public int getErrorCount() {
		return errorTimestamps.size();
	}
}
