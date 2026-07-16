package com.simplebank.bank.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Creates the authentication objects shared by login and request authorization.
 *
 * SecurityConfiguration defines which URLs are protected. This class defines how a username and password are
 * checked when AuthService asks Spring Security to authenticate a login request.
 */
@Configuration
public class ApplicationConfiguration {

	/**
	 * Adapts the application's existing SHA-256 password format to Spring Security's PasswordEncoder contract.
	 *
	 * This keeps previously stored classroom accounts usable. A production migration should replace this encoder
	 * with BCrypt or Argon2 because a fast, unsalted SHA-256 hash is not designed for password storage.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {
			@Override
			public String encode(CharSequence rawPassword) {
				// PasswordHasher returns the same hexadecimal format already stored in the users collection.
				return PasswordHasher.hash(rawPassword.toString());
			}

			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				if (rawPassword == null || encodedPassword == null) {
					// Missing credentials must never be treated as a successful match.
					return false;
				}
				byte[] expected = encode(rawPassword).getBytes(StandardCharsets.UTF_8);
				byte[] actual = encodedPassword.getBytes(StandardCharsets.UTF_8);
				// Constant-time comparison reduces timing differences between matching and non-matching hashes.
				return MessageDigest.isEqual(expected, actual);
			}
		};
	}

	/**
	 * Connects MongoDB-backed UserDetails loading with password verification.
	 *
	 * DaoAuthenticationProvider is used by AuthenticationManager during POST /api/auth/login.
	 */
	@Bean
	public AuthenticationProvider authenticationProvider(
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder
	) {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		return authenticationProvider;
	}

	/**
	 * Exposes Spring's configured AuthenticationManager so AuthService can authenticate login credentials.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
