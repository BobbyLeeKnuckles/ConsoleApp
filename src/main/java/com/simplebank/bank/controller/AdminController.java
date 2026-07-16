package com.simplebank.bank.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Instructor-required sensitive REST endpoint.
 *
 * SecurityConfiguration blocks this controller before invocation unless JwtAuthenticationFilter has established an
 * authenticated principal with ROLE_ADMIN. Keeping authorization in the filter chain avoids duplicating token checks
 * inside every administrator controller method.
 */
@RestController
public class AdminController {

	/**
	 * Confirms that an authorized administrator reached the protected endpoint.
	 */
	@PostMapping("/admin")
	public Map<String, String> admin() {
		// Reaching this method means signature, expiration, user lookup, and ROLE_ADMIN checks all succeeded.
		return Map.of(
				"message", "Admin access granted.",
				"role", "ROLE_ADMIN"
		);
	}
}
