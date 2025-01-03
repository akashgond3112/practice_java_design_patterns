package com.java.design.patterns;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MongoDbConfig {
	private Properties properties;

	public MongoDbConfig() {
		properties = new Properties();
		try (InputStream input = getClass().getClassLoader().getResourceAsStream("mongodb.properties")) {
			if (input != null) {
				properties.load(input);
			}
		} catch (IOException e) {
			// Load defaults if properties file not found
			setDefaults();
		}
	}

	private void setDefaults() {
		properties.setProperty("mongodb.host", "localhost");
		properties.setProperty("mongodb.port", "27017");
		properties.setProperty("mongodb.database", "ecommerce");
		properties.setProperty("mongodb.auth.enabled", "false");
	}

	public String getHost() {
		return properties.getProperty("mongodb.host");
	}

	public int getPort() {
		return Integer.parseInt(properties.getProperty("mongodb.port"));
	}

	public String getDatabase() {
		return properties.getProperty("mongodb.database");
	}

	public String getUsername() {
		return properties.getProperty("mongodb.username");
	}

	public String getPassword() {
		return properties.getProperty("mongodb.password");
	}

	public boolean isAuthEnabled() {
		return Boolean.parseBoolean(properties.getProperty("mongodb.auth.enabled"));
	}

	public String getConnectionString() {
		if (isAuthEnabled() && getUsername() != null && getPassword() != null) {
			return String.format("mongodb://%s:%s@%s:%d", getUsername(), getPassword(), getHost(), getPort());
		}
		return String.format("mongodb://%s:%d", getHost(), getPort());
	}
}
