package com.java.design.patterns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingSidecar implements Sidecar {
	private static final Logger logger = LoggerFactory.getLogger(LoggingSidecar.class);
	private final AtomicBoolean running = new AtomicBoolean(true);

	@Override
	public void run() {
		logger.info("Starting logging sidecar");

		// In a real application, this might watch log files, collect metrics,
		// or provide additional functionality to the main application
		Path logDirectory = Paths.get("logs");

		try {
			if (!Files.exists(logDirectory)) {
				Files.createDirectories(logDirectory);
			}

			// Set up a watch service to monitor log directory
			WatchService watchService = FileSystems.getDefault().newWatchService();
			logDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_MODIFY);

			logger.info("Log monitoring active for directory: {}", logDirectory.toAbsolutePath());

			while (running.get()) {
				WatchKey key;
				try {
					key = watchService.poll(5, java.util.concurrent.TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}

				// Collect metrics every 5 seconds even if no log events
				collectMetrics();

				if (key == null) {
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();

					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}

					@SuppressWarnings("unchecked") WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();

					logger.info("Detected log file event: {} - {}", kind, filename);

					// Here we could parse logs, send them to a monitoring service, etc.
				}

				boolean valid = key.reset();
				if (!valid) {
					break;
				}
			}

		} catch (IOException e) {
			logger.error("Error in logging sidecar", e);
		}

		logger.info("Logging sidecar shutdown complete");
	}

	private void collectMetrics() {
		// Simulate collecting and reporting metrics
		logger.debug("Collecting system metrics");

		// Get JVM memory usage
		Runtime runtime = Runtime.getRuntime();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;

		logger.info("Memory usage - Total: {}MB, Used: {}MB, Free: {}MB", totalMemory / (1024 * 1024),
				usedMemory / (1024 * 1024), freeMemory / (1024 * 1024));
	}

	@Override
	public void stop() {
		logger.info("Stopping logging sidecar");
		running.set(false);
	}
}
