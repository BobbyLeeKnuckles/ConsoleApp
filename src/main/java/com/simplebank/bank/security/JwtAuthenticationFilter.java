package com.simplebank.bank.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads a Bearer JWT once per HTTP request and populates Spring Security's request-local context.
 *
 * A valid token does not directly call a controller. It creates an authenticated principal containing the user's
 * current authorities; SecurityConfiguration then decides whether that principal may access the requested URL.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		// The standard JWT transport is: Authorization: Bearer <signed-token>.
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			// Preserve the older Postman header while Bearer tokens remain the primary convention.
			String legacyToken = request.getHeader("X-Auth-Token");
			if (legacyToken == null || legacyToken.isBlank()) {
				// Public requests are allowed to continue anonymously. Protected routes will later return 403.
				filterChain.doFilter(request, response);
				return;
			}
			jwt = legacyToken.trim();
		} else {
			jwt = authHeader.substring(7).trim();
		}

		try {
			// Extracting the subject also verifies the signature, issuer, token structure, and expiration time.
			username = jwtService.extractUsername(jwt);
		} catch (JwtException | IllegalArgumentException exception) {
			// Treat malformed, tampered, or expired tokens as unauthenticated and let the route rules respond.
			filterChain.doFilter(request, response);
			return;
		}

		// Do not replace authentication that an earlier trusted filter already established for this request.
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				// Reload the user so authorization uses the current database role, not a stale role claim alone.
				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				if (jwtService.isTokenValid(jwt, userDetails)) {
					// A JWT proves identity, so no password credential is retained in the request context.
					UsernamePasswordAuthenticationToken authToken = UsernamePasswordAuthenticationToken.authenticated(
							userDetails,
							null,
							userDetails.getAuthorities()
					);
					// Attach request metadata such as the remote address for downstream auditing or diagnostics.
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					// The authorization rules read this context when evaluating hasRole("ADMIN").
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			} catch (AuthenticationException exception) {
				// A token for a deleted or disabled user remains unauthenticated and fails closed.
			}
		}

		// Authentication only annotates the request; the remaining filters and controller still need to run.
		filterChain.doFilter(request, response);
	}
}
