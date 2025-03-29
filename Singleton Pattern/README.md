# Production-Level Singleton Pattern

## Overview

This repository demonstrates a production-ready implementation of the Singleton pattern in Java. Unlike simple textbook examples, this implementation addresses real-world concerns including thread safety, resource management, configuration reloading, and proper error handling.

The primary class `ConfigurationManager` provides a robust, thread-safe singleton that manages application configuration loaded from external property files. It serves as a practical example of how to implement the Singleton pattern in enterprise applications.

## Features

- **Thread-safe implementation** using double-checked locking and ReadWriteLock
- **Lazy initialization** to create the singleton only when needed
- **Dynamic configuration reloading** without application restart
- **Typed property access** with default values (String, Integer, Boolean)
- **Comprehensive error handling** with custom exceptions
- **Proper logging** of operations and errors
- **High concurrency support** with fine-grained read/write locks

## Usage Example

### Basic Usage

```java
// Initialize the configuration manager (only happens once)
ConfigurationManager config = ConfigurationManager.getInstance("config/application.properties");

// Access configuration values from anywhere in the application
String databaseUrl = config.getProperty("database.url");
String username = config.getProperty("database.username");
int maxConnections = config.getIntProperty("database.max_connections", 10);
boolean enableCaching = config.getBooleanProperty("app.enable_caching", false);
```

### Reloading Configuration

```java
// Check if configuration has changed and reload if necessary
boolean wasReloaded = config.refreshIfNeeded();
if (wasReloaded) {
    System.out.println("Configuration has been updated. Applying changes...");
    // Apply configuration changes to running services
}
```

### Scheduled Reloading

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    boolean reloaded = config.refreshIfNeeded();
    if (reloaded) {
        System.out.println("Configuration has been updated. Applying changes...");
        // Apply configuration changes to running services
    }
}, 1, 1, TimeUnit.MINUTES);
```

## Implementation Details

### Double-Checked Locking Pattern

The implementation uses the double-checked locking pattern to ensure that only one instance is created, even in a multithreaded environment:

```java
public static ConfigurationManager getInstance(String configPath) {
    if (instance == null) {
        synchronized (ConfigurationManager.class) {
            if (instance == null) {
                instance = new ConfigurationManager(configPath);
            }
        }
    }
    return instance;
}
```

### ReadWriteLock for Enhanced Concurrency

We use `ReentrantReadWriteLock` to allow multiple threads to read properties simultaneously while ensuring exclusive access during writes:

```java
public String getProperty(String key) {
    lock.readLock().lock();
    try {
        return configProperties.getProperty(key);
    } finally {
        lock.readLock().unlock();
    }
}
```

### Type-Safe Property Access

The implementation provides type-specific access methods with default values:

```java
public int getIntProperty(String key, int defaultValue) {
    String value = getProperty(key);
    if (value == null) {
        return defaultValue;
    }
    
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        LOGGER.warning("Property " + key + " is not a valid integer: " + value);
        return defaultValue;
    }
}
```

## Testing the Singleton

Testing singletons requires special consideration due to their global state. The included test suite demonstrates:

- Resetting the singleton between tests using reflection
- Creating temporary configuration files
- Testing thread safety and property access
- Verifying configuration reloading behavior
- Testing error conditions

## Design Considerations

### When to Use This Pattern

The Singleton pattern is particularly useful for:

- Configuration management
- Resource pools (database connections, thread pools)
- Logging frameworks
- Cache implementations
- Service managers

### When to Consider Alternatives

While powerful, the Singleton pattern has limitations:

- It can make unit testing more difficult
- It introduces global state which can create hidden dependencies
- It can complicate dependency injection

In modern applications, consider using dependency injection frameworks that provide singleton scope without the direct coupling.

## Requirements

- Java 8 or higher
- JUnit 5 for running tests

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request