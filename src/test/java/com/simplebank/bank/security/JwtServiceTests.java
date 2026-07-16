package com.simplebank.bank.security;

import com.simplebank.bank.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

		String token = jwtService.createToken(testUser());

		assertTrue(jwtService.isValidToken(token));
	}

	@Test
	void createdTokenIsRejectedAfterItExpires() {
		JwtService issuer = new JwtService(STRONG_SECRET, 1, fixedClock(NOW));
		String token = issuer.createToken(testUser());
		JwtService verifier = new JwtService(STRONG_SECRET, 1, fixedClock(NOW.plusSeconds(120)));

		assertFalse(verifier.isValidToken(token));
	}

	@Test
	void weakConfiguredSecretFailsFast() {
		assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 30, fixedClock(NOW)));
	}

	private User testUser() {
		User user = new User("Billy Huynh", "billy@example.com", PasswordHasher.hash("password123"));
		// Mongo normally fills id after save; the test sets it so the JWT subject is realistic.
		ReflectionTestUtils.setField(user, "id", "user-1");
		return user;
	}

	private Clock fixedClock(Instant instant) {
		return Clock.fixed(instant, ZoneOffset.UTC);
	}
}
