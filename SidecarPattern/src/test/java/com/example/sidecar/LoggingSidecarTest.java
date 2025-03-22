package com.example.sidecar;

import com.java.design.patterns.LoggingSidecar;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoggingSidecarTest {

	@Test
	void testSidecarLifecycle() {
		LoggingSidecar sidecar = new LoggingSidecar();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(sidecar);

		// Let the sidecar run briefly
		assertDoesNotThrow(() -> {
			TimeUnit.SECONDS.sleep(1);
			sidecar.stop();
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		});
	}
}
