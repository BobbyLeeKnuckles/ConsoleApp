package com.simplebank.bank.service;

import com.simplebank.bank.dto.LoginRequest;
import com.simplebank.bank.dto.LoginResponse;
import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.UserRepository;
import com.simplebank.bank.security.JwtService;
import com.simplebank.bank.security.PasswordHasher;
import org.springframework.stereotype.Service;

/**
 * Handles login and token validation.
 *
 * Authentication now uses JWTs, so the backend does not need to remember every issued token in memory.
 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
	}

	public LoginResponse login(LoginRequest request) {
		if (request == null || request.email() == null || request.password() == null) {
			throw new IllegalArgumentException("Email and password are required.");
		}
		// Normalize the email so Billy@Example.com and billy@example.com are treated the same.
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
		// Compare password hashes instead of storing or comparing the raw password.
		if (user.getPasswordHash() == null || !user.getPasswordHash().equals(PasswordHasher.hash(request.password()))) {
			throw new IllegalArgumentException("Invalid email or password.");
		}
		// JWTs are signed, time-limited tokens that the client sends with future protected requests.
		String token = jwtService.createToken(user);
		return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), jwtService.expiresAt());
	}

	public boolean isValidToken(String token) {
		// The interceptor calls this before protected API requests reach the controller.
		return jwtService.isValidToken(token);
	}
}
