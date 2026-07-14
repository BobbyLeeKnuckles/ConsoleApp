package com.simplebank.bank.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for a bank customer.
 *
 * One user can own multiple accounts, and login uses the user's email/password hash.
 */
@Document("users")
public class User {

	@Id
	private String id;

	private String name;

	// MongoDB enforces uniqueness so two users cannot register the same email.
	@Indexed(unique = true)
	private String email;

	// Store the password hash, never the original plain-text password.
	private String passwordHash;

	@CreatedDate
	private Instant createdAt;

	public User() {
	}

	public User(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public User(String name, String email, String passwordHash) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updatePasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}
