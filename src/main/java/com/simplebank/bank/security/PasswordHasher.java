package com.simplebank.bank.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Small password hashing helper used before saving or comparing passwords.
 *
 * For a production app, use BCrypt/Argon2 through Spring Security. SHA-256 is kept here for beginner readability.
 */
public final class PasswordHasher {

	private PasswordHasher() {
		// Utility class: callers use the static hash method and should not create instances.
	}

	/**
	 * Produces the deterministic hexadecimal SHA-256 format used by existing classroom user records.
	 *
	 * @param password raw password received during registration or authentication
	 * @return lowercase hexadecimal hash suitable for storage; the original password cannot be recovered from it
	 */
	public static String hash(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			// UTF-8 makes the same password produce the same bytes on every supported operating system.
			byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			// Store the hash as text so it fits cleanly in MongoDB.
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException exception) {
			// SHA-256 is required by Java, so this indicates a broken runtime rather than bad user input.
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}
}
