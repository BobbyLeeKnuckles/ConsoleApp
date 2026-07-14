package com.simplebank.bank.service;

import com.simplebank.bank.dto.LoginRequest;
import com.simplebank.bank.dto.LoginResponse;
import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.UserRepository;
import com.simplebank.bank.security.PasswordHasher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles login and token validation.
 *
 * This is intentionally simple for the project: tokens live in memory and disappear when the app restarts.
 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	// Maps auth token -> user id. ConcurrentHashMap is safe for multiple web requests at the same time.
	private final Map<String, String> tokens = new ConcurrentHashMap<>();

	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public LoginResponse login(LoginRequest request) {
		if (request == null || request.email() == null || request.password() == null) {
			throw new IllegalArgumentException("Email and password are required.");
		}
		// Normalize the email so Hoang@Example.com and hoang@example.com are treated the same.
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
		// Compare password hashes instead of storing or comparing the raw password.
		if (user.getPasswordHash() == null || !user.getPasswordHash().equals(PasswordHasher.hash(request.password()))) {
			throw new IllegalArgumentException("Invalid email or password.");
		}
		// UUID gives us a random token that is hard to guess for this beginner project.
		String token = UUID.randomUUID().toString();
		tokens.put(token, user.getId());
		return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
	}

	public boolean isValidToken(String token) {
		// The interceptor calls this before protected API requests reach the controller.
		return token != null && tokens.containsKey(token);
	}
}
