package com.simplebank.bank.service;

import com.simplebank.bank.dto.LoginRequest;
import com.simplebank.bank.dto.LoginResponse;
import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.UserRepository;
import com.simplebank.bank.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Coordinates username/password login and JWT creation.
 *
 * Credential verification is delegated to Spring Security. After authentication succeeds, this service returns a
 * signed, time-limited token, so the backend does not need to store an in-memory login session.
 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthService(
			UserRepository userRepository,
			JwtService jwtService,
			AuthenticationManager authenticationManager
	) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	/**
	 * Authenticates a login request and returns the token plus safe user details needed by the frontend.
	 */
	public LoginResponse login(LoginRequest request) {
		// Reject incomplete requests before calling the authentication framework.
		if (request == null || request.email() == null || request.password() == null) {
			throw new IllegalArgumentException("Email and password are required.");
		}
		// Authentication and later JWT subject comparisons must use the same normalized username.
		String email = request.email().trim().toLowerCase();
		Authentication authentication;
		try {
			// Delegate credential checks to Spring Security's configured AuthenticationProvider.
			authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(email, request.password())
			);
		} catch (AuthenticationException exception) {
			// Use one response for unknown users and bad passwords to avoid leaking which accounts exist.
			throw new IllegalArgumentException("Invalid email or password.");
		}

		// Load the domain user separately because LoginResponse includes database fields not exposed by UserDetails.
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
		// The authenticated principal carries the ROLE_USER or ROLE_ADMIN authority embedded in the JWT.
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String token = jwtService.generateToken(userDetails);
		return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), jwtService.expiresAt());
	}
}
