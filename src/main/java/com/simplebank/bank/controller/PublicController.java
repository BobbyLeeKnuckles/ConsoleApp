package com.simplebank.bank.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Simple regular endpoint used to demonstrate that non-sensitive routes need no JWT.
 */
@RestController
public class PublicController {

	@GetMapping("/public")
	public Map<String, String> publicEndpoint() {
		return Map.of("message", "Public access granted. No token required.");
	}
}
