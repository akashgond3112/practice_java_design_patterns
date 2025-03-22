package com.java.design.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

public class MainApplication {
	private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);
	private static final long DEFAULT_RUNTIME_SECONDS = 30; // Default runtime of 30 seconds

	public static void main(String[] args) {
		logger.info("Starting main application...");

		// Parse runtime from command line arguments if provided
		long runtimeSeconds = DEFAULT_RUNTIME_SECONDS;
		if (args.length > 0) {
			try {
				runtimeSeconds = Long.parseLong(args[0]);
				logger.info("Setting application runtime to {} seconds", runtimeSeconds);
			} catch (NumberFormatException e) {
				logger.warn("Invalid runtime argument: {}. Using default of {} seconds", args[0],
						DEFAULT_RUNTIME_SECONDS);
			}
		} else {
			logger.info("No runtime specified. Application will run for {} seconds", DEFAULT_RUNTIME_SECONDS);
		}

		// Create a countdown latch for controlled shutdown
		CountDownLatch shutdownLatch = new CountDownLatch(1);

		// Create and start the sidecar
		Sidecar sidecar = new LoggingSidecar();
		Thread sidecarThread = new Thread(sidecar);
		sidecarThread.setDaemon(true);
		sidecarThread.start();

		// Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Shutdown hook triggered. Cleaning up resources...");
			performCleanShutdown(null, sidecar);
		}));

		// Simulate the main application's business logic
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(() -> {
			try {
				BusinessService service = new BusinessService();
				service.processRequest();
			} catch (Exception e) {
				logger.error("Error in business logic", e);
			}
		}, 0, 5, TimeUnit.SECONDS);

		// Schedule application shutdown after the specified runtime
		long finalRuntimeSeconds = runtimeSeconds;
		Thread shutdownThread = new Thread(() -> {
			try {
				logger.info("Application will run for {} seconds", finalRuntimeSeconds);
				Thread.sleep(finalRuntimeSeconds * 1000);
				logger.info("Reached configured runtime limit of {} seconds. Initiating shutdown...",
						finalRuntimeSeconds);
				performCleanShutdown(executor, sidecar);
				shutdownLatch.countDown();
			} catch (InterruptedException e) {
				logger.error("Shutdown thread interrupted", e);
				Thread.currentThread().interrupt();
			}
		});
		shutdownThread.setDaemon(false);
		shutdownThread.start();

		try {
			// Wait for the shutdown signal
			shutdownLatch.await();
			logger.info("Main thread exiting");
		} catch (InterruptedException e) {
			logger.error("Main thread interrupted while waiting for shutdown", e);
			Thread.currentThread().interrupt();
		}
	}

	private static void performCleanShutdown(ScheduledExecutorService executor, Sidecar sidecar) {
		logger.info("Performing clean shutdown...");

		// Stop the executor if it exists
		if (executor != null && !executor.isShutdown()) {
			try {
				executor.shutdown();
				if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
					executor.shutdownNow();
				}
				logger.info("Executor service shut down");
			} catch (InterruptedException e) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
				logger.warn("Executor shutdown interrupted");
			}
		}

		// Stop the sidecar
		if (sidecar != null) {
			sidecar.stop();
			logger.info("Sidecar stopped");
		}

		logger.info("Shutdown complete. Exiting application.");
	}
}
