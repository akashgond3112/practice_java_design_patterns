package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductCacheStore {
	private static final int CAPACITY = 100;
	private static LruCache cache;
	private final DbManager dbManager;

	public ProductCacheStore(DbManager dbManager) {
		this.dbManager = dbManager;
		cache = new LruCache(CAPACITY);
	}

	public Product readThrough(String productId) {
		if (cache.get(productId) != null) {
			log.info("Cache hit for product: {}", productId);
			return cache.get(productId);
		}
		log.info("Cache miss for product: {}. Fetching from database.", productId);
		Product product = dbManager.readFromDb(productId);
		if (product != null) {
			cache.set(productId, product);
		}
		return product;
	}

	public void writeThrough(Product product) {
		dbManager.writeToDb(product);
		cache.set(product.getProductId(), product);
		log.info("Product {} written to both cache and database", product.getProductId());
	}

	public void writeAround(Product product) {
		dbManager.writeToDb(product);
		// Invalidate cache entry if it exists
		if (cache.get(product.getProductId()) != null) {
			cache.set(product.getProductId(), null);
			log.info("Cache entry invalidated for product: {}", product.getProductId());
		}
	}
}