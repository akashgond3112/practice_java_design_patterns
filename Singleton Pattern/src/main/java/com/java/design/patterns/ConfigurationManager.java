package com.java.design.patterns;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread-safe Singleton Configuration Manager for production environments. This class loads application configuration
 * from a file and provides global access to configuration properties throughout the application.
 */
public class ConfigurationManager {
	// Private static instance - the only one that will exist
	private static volatile ConfigurationManager instance;

	// Internal configuration storage
	private final Properties configProperties;

	// Thread synchronization mechanism
	private final ReadWriteLock lock;

	// Path to the configuration file
	private final Path configFilePath;

	// Last modified timestamp to detect changes
	private long lastModifiedTime;

	// Logger for error reporting
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class.getName());

	/**
	 * Private constructor prevents instantiation from outside
	 *
	 * @param configPath
	 * 		Path to the configuration file
	 */
	private ConfigurationManager(String configPath) {
		this.configProperties = new Properties();
		this.lock = new ReentrantReadWriteLock();
		this.configFilePath = Paths.get(configPath);

		loadConfiguration();
	}

	/**
	 * Gets the singleton instance, creating it if necessary
	 *
	 * @param configPath
	 * 		Path to the configuration file
	 * @return The singleton ConfigurationManager instance
	 */
	public static ConfigurationManager getInstance(String configPath) {
		// Double-checked locking pattern
		if (instance == null) {
			// Synchronize on class to prevent multiple threads from creating instances
			synchronized (ConfigurationManager.class) {
				if (instance == null) {
					instance = new ConfigurationManager(configPath);
				}
			}
		}
		return instance;
	}

	/**
	 * Gets the singleton instance using the previously specified config path
	 *
	 * @return The singleton ConfigurationManager instance
	 * @throws IllegalStateException
	 * 		if getInstance(String) hasn't been called first
	 */
	public static ConfigurationManager getInstance() {
		if (instance == null) {
			throw new IllegalStateException(
					"Configuration manager not initialized. Call getInstance(String configPath) first.");
		}
		return instance;
	}

	/**
	 * Loads configuration from the specified file
	 */
	private void loadConfiguration() {
		lock.writeLock().lock();
		try (FileInputStream fis = new FileInputStream(configFilePath.toFile())) {
			configProperties.clear();
			configProperties.load(fis);
			lastModifiedTime = Files.getLastModifiedTime(configFilePath).toMillis();
			LOGGER.info("Configuration loaded successfully from {}", configFilePath);
		} catch (IOException e) {
			LOGGER.info("Failed to load configuration {} {} ", e, Level.SEVERE);
			throw new ConfigurationException("Could not load configuration from " + configFilePath, e);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Checks if the configuration file has been modified and reloads if necessary
	 *
	 * @return true if configuration was reloaded
	 */
	public boolean refreshIfNeeded() {
		try {
			long currentModifiedTime = Files.getLastModifiedTime(configFilePath).toMillis();
			if (currentModifiedTime > lastModifiedTime) {
				loadConfiguration();
				return true;
			}
			return false;
		} catch (IOException e) {
			LOGGER.warn("Failed to check configuration file modification time", e);
			return false;
		}
	}

	/**
	 * Gets a configuration property
	 *
	 * @param key
	 * 		The property key
	 * @return The property value or null if not found
	 */
	public String getProperty(String key) {
		lock.readLock().lock();
		try {
			return configProperties.getProperty(key);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets a configuration property with a default value
	 *
	 * @param key
	 * 		The property key
	 * @param defaultValue
	 * 		The default value to return if the key is not found
	 * @return The property value or the default if not found
	 */
	public String getProperty(String key, String defaultValue) {
		lock.readLock().lock();
		try {
			return configProperties.getProperty(key, defaultValue);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets an integer property
	 *
	 * @param key
	 * 		The property key
	 * @param defaultValue
	 * 		The default value to return if the key is not found or not an integer
	 * @return The property value as an integer or the default
	 */
	public int getIntProperty(String key, int defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			LOGGER.warn("Property {} is not a valid integer: {}", key, value);
			return defaultValue;
		}
	}

	/**
	 * Gets a boolean property
	 *
	 * @param key
	 * 		The property key
	 * @param defaultValue
	 * 		The default value to return if the key is not found
	 * @return The property value as a boolean or the default
	 */
	public boolean getBooleanProperty(String key, boolean defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}

		return Boolean.parseBoolean(value);
	}

	/**
	 * Custom exception for configuration errors
	 */
	public static class ConfigurationException extends RuntimeException {
		public ConfigurationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
