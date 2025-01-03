package com.java.design.patterns;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Product {
	private String productId;
	private String name;
	private double price;
	private boolean inStock;
}

