package com.simplebank.bank.dto;

import java.time.Instant;

/**
 * JSON response returned after successful login.
 */
public record LoginResponse(String token, String userId, String name, String email, Instant expiresAt) {
	// The frontend stores token in sessionStorage and sends it as an Authorization Bearer token.
}
