package com.java.design.patterns;

public interface DbManager {
	void connect();
	void disconnect();
	Product readFromDb(String productId);
	Product writeToDb(Product product);
	Product updateDb(Product product);
	Product upsertDb(Product product);
}
