package com.example.demo.bank.service;

import com.example.demo.bank.dto.LoginRequest;
import com.example.demo.bank.dto.LoginResponse;
import com.example.demo.bank.model.User;
import com.example.demo.bank.repository.UserRepository;
import com.example.demo.bank.security.PasswordHasher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final Map<String, String> tokens = new ConcurrentHashMap<>();

	public AuthService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public LoginResponse login(LoginRequest request) {
		if (request == null || request.email() == null || request.password() == null) {
			throw new IllegalArgumentException("Email and password are required.");
		}
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
		if (user.getPasswordHash() == null || !user.getPasswordHash().equals(PasswordHasher.hash(request.password()))) {
			throw new IllegalArgumentException("Invalid email or password.");
		}
		String token = UUID.randomUUID().toString();
		tokens.put(token, user.getId());
		return new LoginResponse(token, user.getId(), user.getName(), user.getEmail());
	}

	public boolean isValidToken(String token) {
		return token != null && tokens.containsKey(token);
	}
}
