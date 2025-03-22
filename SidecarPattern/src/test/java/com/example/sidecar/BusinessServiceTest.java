package com.example.sidecar;

import com.java.design.patterns.BusinessService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BusinessServiceTest {

	@Test
	void testProcessRequest() {
		BusinessService service = new BusinessService();
		// Since the method has random behavior, we're just testing that it generally works
		assertDoesNotThrow(service::processRequest);
	}
}
