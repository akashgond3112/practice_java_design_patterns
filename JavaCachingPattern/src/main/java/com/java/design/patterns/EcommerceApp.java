package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcommerceApp {
	public static void main(String[] args) {
		MongoDbConfig config = new MongoDbConfig();
		DbManager dbManager = new MongoDbManager(config);
		dbManager.connect();
		
		ProductCacheStore cacheStore = new ProductCacheStore(dbManager);

		// Example usage
		Product laptop = new Product("PROD001", "Gaming Laptop", 1299.99, true);

		// Write-through example
		cacheStore.writeThrough(laptop);

		// Read operations will be faster now
		Product cachedProduct = cacheStore.readThrough("PROD001");
		log.info("Retrieved product: {}", cachedProduct);

		// Update product price
		laptop.setPrice(1199.99);
		cacheStore.writeThrough(laptop);

		// Read updated product
		Product updatedProduct = cacheStore.readThrough("PROD001");
		log.info("Retrieved updated product: {}", updatedProduct);
		
		dbManager.disconnect();
	}
}
