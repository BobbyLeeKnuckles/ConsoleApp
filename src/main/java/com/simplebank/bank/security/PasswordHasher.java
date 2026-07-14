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
	}

	public static String hash(String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			// Store the hash as text so it fits cleanly in MongoDB.
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available.", exception);
		}
	}
}
