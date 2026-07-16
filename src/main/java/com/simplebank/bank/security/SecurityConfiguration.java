package com.simplebank.bank.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Defines the application's URL authorization policy and stateless JWT filter chain.
 *
 * Regular endpoints remain public for the instructor exercise. Only /admin and its child paths require an
 * authenticated principal whose authorities contain ROLE_ADMIN.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;

	public SecurityConfiguration(
			JwtAuthenticationFilter jwtAuthFilter,
			AuthenticationProvider authenticationProvider
	) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.authenticationProvider = authenticationProvider;
	}

	/**
	 * Builds the ordered servlet-security pipeline applied to every incoming HTTP request.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// JWT authentication is stateless and is not based on a browser session cookie, so CSRF is disabled.
				.csrf(AbstractHttpConfigurer::disable)
				// Reuse WebConfig's CORS rules for the React development server.
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						// Browser preflight requests contain no JWT and must reach the configured CORS policy.
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						// The instructor-required sensitive endpoint is restricted to administrators.
						.requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
						// Every endpoint outside the admin namespace remains publicly accessible.
						.anyRequest().permitAll()
				)
				// Never create an HTTP session; every protected request must prove itself with a JWT.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Use the MongoDB UserDetailsService and configured password encoder for login authentication.
				.authenticationProvider(authenticationProvider)
				.exceptionHandling(exceptions -> exceptions
						// Missing, malformed, or expired credentials reach this entry point.
						// The instructor requires status 403 and "Access Forbidden" instead of the usual 401 response.
						.authenticationEntryPoint((request, response, exception) -> writeError(
								response,
								HttpServletResponse.SC_FORBIDDEN,
								"Forbidden",
								"Access Forbidden."
						))
						// An authenticated ROLE_USER reaches this handler when attempting an administrator route.
						.accessDeniedHandler((request, response, exception) -> writeError(
								response,
								HttpServletResponse.SC_FORBIDDEN,
								"Forbidden",
								"Access Forbidden."
						))
				)
				// This API issues JWTs through /api/auth/login, so built-in login mechanisms are unnecessary.
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				// Authenticate Bearer tokens before Spring's username/password filter evaluates authorization.
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * Produces one consistent JSON error shape for both anonymous and under-authorized requests.
	 */
	private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(
				"{\"status\":" + status + ",\"error\":\"" + error + "\",\"message\":\"" + message + "\"}"
		);
	}
}
