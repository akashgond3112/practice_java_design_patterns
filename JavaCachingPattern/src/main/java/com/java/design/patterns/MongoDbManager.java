package com.java.design.patterns;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoDbManager implements DbManager {
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<Document> products;
	private final MongoDbConfig config;

	public MongoDbManager(MongoDbConfig config) {
		this.config = config;
	}

	@Override
	public void connect() {
		try {
			mongoClient = MongoClients.create(config.getConnectionString());
			database = mongoClient.getDatabase(config.getDatabase());
			products = database.getCollection("products");
			log.info("Successfully connected to MongoDB at {}", config.getHost());
		} catch (Exception e) {
			log.error("Failed to connect to MongoDB: {}", e.getMessage());
			throw new RuntimeException("Database connection failed", e);
		}
	}

	@Override
	public void disconnect() {
		if (mongoClient != null) {
			mongoClient.close();
			log.info("Disconnected from MongoDB");
		}
	}

	@Override
	public Product readFromDb(String productId) {
		try {
			Document doc = products.find(new Document("productId", productId)).first();
			if (doc == null) {
				log.info("Product not found in database: {}", productId);
				return null;
			}
			return documentToProduct(doc);
		} catch (Exception e) {
			log.error("Error reading product from database: {}", e.getMessage());
			throw new RuntimeException("Database read failed", e);
		}
	}

	@Override
	public Product writeToDb(Product product) {
		try {
			Document doc = productToDocument(product);
			products.insertOne(doc);
			log.info("Successfully wrote product to database: {}", product.getProductId());
			return product;
		} catch (Exception e) {
			log.error("Error writing product to database: {}", e.getMessage());
			throw new RuntimeException("Database write failed", e);
		}
	}

	@Override
	public Product updateDb(Product product) {
		try {
			Document query = new Document("productId", product.getProductId());
			Document update = productToDocument(product);
			products.replaceOne(query, update);
			log.info("Successfully updated product in database: {}", product.getProductId());
			return product;
		} catch (Exception e) {
			log.error("Error updating product in database: {}", e.getMessage());
			throw new RuntimeException("Database update failed", e);
		}
	}

	@Override
	public Product upsertDb(Product product) {
		try {
			Document query = new Document("productId", product.getProductId());
			Document update = productToDocument(product);
			products.replaceOne(query, update, new com.mongodb.client.model.ReplaceOptions().upsert(true));
			log.info("Successfully upserted product in database: {}", product.getProductId());
			return product;
		} catch (Exception e) {
			log.error("Error upserting product in database: {}", e.getMessage());
			throw new RuntimeException("Database upsert failed", e);
		}
	}

	private Document productToDocument(Product product) {
		return new Document().append("productId", product.getProductId()).append("name", product.getName())
				.append("price", product.getPrice()).append("inStock", product.isInStock());
	}

	private Product documentToProduct(Document doc) {
		return new Product(doc.getString("productId"), doc.getString("name"), doc.getDouble("price"),
				doc.getBoolean("inStock"));
	}
}
