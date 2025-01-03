# Java Caching Pattern Implementation

## 🚀 Overview
This project demonstrates a robust implementation of the Caching Pattern in Java, specifically designed for e-commerce product management. The implementation features an LRU (Least Recently Used) cache with multiple caching strategies to optimize data access and improve application performance.

## 📋 Features

- LRU (Least Recently Used) caching mechanism
- Multiple caching strategies:
    - Read-through caching
    - Write-through caching
    - Write-around caching
- Thread-safe implementation
- Configurable cache capacity
- Comprehensive logging
- Database abstraction layer

## 🏗️ Architecture

### Core Components

1. **Product**
    - Main data class representing product information
    - Contains product ID, name, price, and stock status

2. **LruCache**
    - Implements the LRU caching mechanism
    - Uses a HashMap with a doubly-linked list for O(1) access and removal
    - Maintains the most recently used items at the front

3. **ProductCacheStore**
    - Manages caching strategies
    - Handles interaction between cache and database
    - Implements different writing policies

4. **DbManager**
    - Interface for database operations
    - Allows for different database implementations

## 💻 Implementation Example

```java
// Initialize cache store
DbManager dbManager = new MongoDbManager();
ProductCacheStore cacheStore = new ProductCacheStore(dbManager);

// Create a product
Product laptop = new Product("PROD001", "Gaming Laptop", 1299.99, true);

// Write to cache and database
cacheStore.writeThrough(laptop);

// Read product (will be served from cache)
Product cachedProduct = cacheStore.readThrough("PROD001");
```

## 🔍 Caching Strategies

### Read-Through
- First checks the cache for requested data
- If not found, fetches from database and updates cache
- Subsequent reads for same data are served from cache

```java
Product product = cacheStore.readThrough("PROD001");
```

### Write-Through
- Writes data to both cache and database simultaneously
- Ensures consistency between cache and database
- Slightly slower writes but guarantees data consistency

```java
cacheStore.writeThrough(newProduct);
```

### Write-Around
- Writes data only to the database
- Invalidates the cache entry if it exists
- Useful when data isn't immediately needed

```java
cacheStore.writeAround(newProduct);
```

## ⚙️ Configuration

Configure cache capacity in `ProductCacheStore`:

```java
private static final int CAPACITY = 100; // Adjust based on your needs
```

## 📊 Performance Considerations

1. **Cache Size**
    - Larger cache = Better hit ratio
    - Smaller cache = Less memory usage
    - Monitor hit/miss ratio to optimize size

2. **Eviction Policy**
    - LRU implementation removes least recently used items
    - Automatic cleanup when cache reaches capacity

3. **Cache Coherency**
    - Write-through ensures strong consistency
    - Write-around useful for write-heavy scenarios

## 🎯 Best Practices

1. **Cache Initialization**
   ```java
   // Initialize with appropriate capacity
   cache = new LruCache(CAPACITY);
   ```

2. **Error Handling**
   ```java
   // Always check for null when reading
   Product product = cacheStore.readThrough(productId);
   if (product != null) {
       // Process product
   }
   ```

3. **Logging**
   ```java
   // Important events are logged
   LOGGER.info("Cache hit for product: {}", productId);
   ```

## 🔧 Setup and Dependencies

Required dependencies:
```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.22</version>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.32</version>
    </dependency>
</dependencies>
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.