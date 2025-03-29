package com.java.design.patterns;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationManagerTest {

	@TempDir
	Path tempDir;

	private File configFile;

	@BeforeEach
	void setUp() throws IOException {
		// Create a temporary configuration file for testing
		configFile = tempDir.resolve("test-config.properties").toFile();

		Properties props = new Properties();
		props.setProperty("test.string", "test-value");
		props.setProperty("test.int", "42");
		props.setProperty("test.boolean", "true");

		try (FileWriter writer = new FileWriter(configFile)) {
			props.store(writer, "Test Configuration");
		}

		// Reset the singleton instance before each test
		resetSingleton();
	}

	@AfterEach
	void tearDown() {
		// Reset the singleton instance after each test
		resetSingleton();
	}

	/**
	 * Resets the singleton instance using reflection This is necessary for testing to avoid test interdependence
	 */
	private void resetSingleton() {
		try {
			Field instance = ConfigurationManager.class.getDeclaredField("instance");
			instance.setAccessible(true);
			instance.set(null, null);
		} catch (Exception e) {
			throw new RuntimeException("Failed to reset singleton instance", e);
		}
	}

	@Test
	void testSingletonInstanceIsTheSame() {
		ConfigurationManager instance1 = ConfigurationManager.getInstance(configFile.getAbsolutePath());
		ConfigurationManager instance2 = ConfigurationManager.getInstance();

		assertSame(instance1, instance2, "The two instances should be the same object");
	}

	@Test
	void testPropertiesAreLoadedCorrectly() {
		ConfigurationManager config = ConfigurationManager.getInstance(configFile.getAbsolutePath());

		assertEquals("test-value", config.getProperty("test.string"));
		assertEquals(42, config.getIntProperty("test.int", 0));
		assertTrue(config.getBooleanProperty("test.boolean", false));
	}

	@Test
	void testDefaultValuesAreUsed() {
		ConfigurationManager config = ConfigurationManager.getInstance(configFile.getAbsolutePath());

		assertEquals("default", config.getProperty("non.existent", "default"));
		assertEquals(100, config.getIntProperty("non.existent", 100));
		assertTrue(config.getBooleanProperty("non.existent", true));
	}

	@Test
	void testConfigurationRefresh() throws IOException, InterruptedException {
		ConfigurationManager config = ConfigurationManager.getInstance(configFile.getAbsolutePath());

		// Initial value
		assertEquals("test-value", config.getProperty("test.string"));

		// Wait a moment to ensure the file timestamp will be different
		Thread.sleep(100);

		// Update the configuration file
		Properties props = new Properties();
		props.setProperty("test.string", "updated-value");
		props.setProperty("test.int", "42");
		props.setProperty("test.boolean", "true");

		try (FileWriter writer = new FileWriter(configFile)) {
			props.store(writer, "Updated Test Configuration");
		}

		// Verify that the configuration is refreshed
		assertTrue(config.refreshIfNeeded());
		assertEquals("updated-value", config.getProperty("test.string"));
	}

	@Test
	void testExceptionOnMissingFile() {
		assertThrows(ConfigurationManager.ConfigurationException.class, () -> {
			ConfigurationManager.getInstance("non-existent-file.properties");
		});
	}

	@Test
	void testExceptionWhenGetInstanceCalledWithoutInit() {
		assertThrows(IllegalStateException.class, () -> {
			// This should fail because we haven't initialized with a config path first
			ConfigurationManager.getInstance();
		});
	}
}
