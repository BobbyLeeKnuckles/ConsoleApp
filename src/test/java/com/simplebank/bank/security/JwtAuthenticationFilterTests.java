package com.simplebank.bank.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for converting an Authorization Bearer token into request-scoped Spring authentication.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTests {

	@Mock
	private JwtService jwtService;

	@Mock
	private UserDetailsService userDetailsService;

	private JwtAuthenticationFilter jwtAuthFilter;

	@BeforeEach
	void setUp() {
		jwtAuthFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void validBearerTokenAuthenticatesTheRequest() throws ServletException, IOException {
		// UserDetails represents the current database authorities loaded after the JWT subject is verified.
		UserDetails userDetails = org.springframework.security.core.userdetails.User
				.withUsername("billy@example.com")
				.password("password-hash")
				.roles("USER")
				.build();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer signed-token");

		when(jwtService.extractUsername("signed-token")).thenReturn("billy@example.com");
		when(userDetailsService.loadUserByUsername("billy@example.com")).thenReturn(userDetails);
		when(jwtService.isTokenValid("signed-token", userDetails)).thenReturn(true);

		jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

		assertEquals("billy@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
		assertEquals("ROLE_USER", SecurityContextHolder.getContext().getAuthentication().getAuthorities()
				.iterator().next().getAuthority());
	}

	@Test
	void requestWithoutTokenContinuesWithoutAuthentication() throws ServletException, IOException {
		jwtAuthFilter.doFilter(
				new MockHttpServletRequest(),
				new MockHttpServletResponse(),
				new MockFilterChain()
		);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
		verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
	}
}
