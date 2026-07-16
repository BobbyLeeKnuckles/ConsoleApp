package com.simplebank.bank.dto;

/**
 * JSON request body for login.
 */
public record LoginRequest(String email, String password) {
	// The service validates both fields before attempting authentication.
}
