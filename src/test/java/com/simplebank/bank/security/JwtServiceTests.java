package com.simplebank.bank.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for JWT behavior that should stay true even if the controller layer changes.
 */
class JwtServiceTests {

	private static final String STRONG_SECRET = "12345678901234567890123456789012";
	private static final Instant NOW = Instant.parse("2026-07-15T12:00:00Z");

	@Test
	void createdTokenIsValidBeforeItExpires() {
		JwtService jwtService = new JwtService(STRONG_SECRET, 30, fixedClock(NOW));

		UserDetails userDetails = testUser();
		String token = jwtService.generateToken(userDetails);

		assertTrue(jwtService.isTokenValid(token, userDetails));
		assertEquals("billy@example.com", jwtService.extractUsername(token));
	}

	@Test
	void createdTokenIsRejectedAfterItExpires() {
		JwtService issuer = new JwtService(STRONG_SECRET, 1, fixedClock(NOW));
		UserDetails userDetails = testUser();
		String token = issuer.generateToken(userDetails);
		JwtService verifier = new JwtService(STRONG_SECRET, 1, fixedClock(NOW.plusSeconds(120)));

		assertFalse(verifier.isTokenValid(token, userDetails));
	}

	@Test
	void weakConfiguredSecretFailsFast() {
		assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 30, fixedClock(NOW)));
	}

	private UserDetails testUser() {
		return org.springframework.security.core.userdetails.User
				.withUsername("billy@example.com")
				.password(PasswordHasher.hash("password123"))
				.roles("USER")
				.build();
	}

	private Clock fixedClock(Instant instant) {
		return Clock.fixed(instant, ZoneOffset.UTC);
	}
}
