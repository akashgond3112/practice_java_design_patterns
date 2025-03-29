package com.java.design.patterns;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
	// Logger for error reporting
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class.getName());

	public static void main(String[] args) {
		// Initialize the configuration manager - only needs to happen once
		try {
			ConfigurationManager config = ConfigurationManager.getInstance("config/application.properties");

			// Now we can access configuration values from anywhere in the application
			String databaseUrl = config.getProperty("database.url");
			String username = config.getProperty("database.username");
			int maxConnections = config.getIntProperty("database.max_connections", 10);
			boolean enableCaching = config.getBooleanProperty("app.enable_caching", false);

			LOGGER.info("Starting application with configuration:");
			LOGGER.info("Database URL: {}", databaseUrl);
			LOGGER.info("User Name: {}", username);
			LOGGER.info("Max connections: {}", maxConnections);
			LOGGER.info("Caching enabled: {}", enableCaching);

			// Start the application...
			startApplicationServices();

		} catch (ConfigurationManager.ConfigurationException e) {
			LOGGER.error("Failed to initialize application: {}", e.getMessage());
			System.exit(1);
		}
	}

	private static void startApplicationServices() {
		// Access singleton from other parts of the application
		ConfigurationManager config = ConfigurationManager.getInstance();

		// Use configuration values to initialize services
		// ...

		// Example: Periodically check for configuration changes
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
			boolean reloaded = config.refreshIfNeeded();
			if (reloaded) {
				LOGGER.info("Configuration has been updated. Applying changes...");
				// Apply configuration changes to running services
				// ...
			}
		}, 1, 1, TimeUnit.MINUTES);
	}
}
