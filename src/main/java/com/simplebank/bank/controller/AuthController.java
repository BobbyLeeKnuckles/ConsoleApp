package com.simplebank.bank.controller;

import com.simplebank.bank.dto.LoginRequest;
import com.simplebank.bank.dto.LoginResponse;
import com.simplebank.bank.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API.
 *
 * Login is separated from account actions so clients can get a token before calling protected endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	/**
	 * Public login endpoint. Successful credentials return the Bearer token used for POST /admin.
	 */
	@PostMapping("/login")
	public LoginResponse login(@RequestBody LoginRequest request) {
		// Returns a signed JWT that the frontend sends back as an Authorization Bearer token.
		return authService.login(request);
	}
}
