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

	// Spring Security converts this value into ROLE_USER or ROLE_ADMIN.
	private UserRole role = UserRole.USER;

	@CreatedDate
	private Instant createdAt;

	public User() {
	}

	public User(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public User(String name, String email, String passwordHash) {
		this(name, email, passwordHash, UserRole.USER);
	}

	public User(String name, String email, String passwordHash, UserRole role) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role == null ? UserRole.USER : role;
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

	public UserRole getRole() {
		// Existing MongoDB users created before roles were added default to USER.
		return role == null ? UserRole.USER : role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void updateName(String name) {
		// Keep mutation methods small so the service controls validation before changing state.
		this.name = name;
	}

	public void updatePasswordHash(String passwordHash) {
		// Used when upgrading older sample users that did not originally have login passwords.
		this.passwordHash = passwordHash;
	}

	public void updateRole(UserRole role) {
		this.role = role == null ? UserRole.USER : role;
	}
}
