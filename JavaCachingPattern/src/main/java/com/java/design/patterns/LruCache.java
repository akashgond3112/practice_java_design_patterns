package com.java.design.patterns;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LruCache {

	static class Node {
		String productId;
		Product product;
		Node previous;
		Node next;

		public Node(String productId, Product product) {
			this.productId = productId;
			this.product = product;
		}
	}

	private final int capacity;
	private final Map<String, Node> cache = new HashMap<>();
	private Node head;
	private Node end;

	public LruCache(int capacity) {
		this.capacity = capacity;
	}

	public Product get(String productId) {
		if (cache.containsKey(productId)) {
			Node node = cache.get(productId);
			remove(node);
			setHead(node);
			return node.product;
		}
		return null;
	}

	public void set(String productId, Product product) {
		if (cache.containsKey(productId)) {
			Node old = cache.get(productId);
			old.product = product;
			remove(old);
			setHead(old);
		} else {
			Node newNode = new Node(productId, product);
			if (cache.size() >= capacity) {
				log.info("Cache full! Removing least recently used product: {}", end.productId);
				cache.remove(end.productId);
				remove(end);
				setHead(newNode);
			} else {
				setHead(newNode);
			}
			cache.put(productId, newNode);
		}
	}

	private void remove(Node node) {
		if (node.previous != null) {
			node.previous.next = node.next;
		} else {
			head = node.next;
		}
		if (node.next != null) {
			node.next.previous = node.previous;
		} else {
			end = node.previous;
		}
	}

	private void setHead(Node node) {
		node.next = head;
		node.previous = null;
		if (head != null) {
			head.previous = node;
		}
		head = node;
		if (end == null) {
			end = head;
		}
	}
}

