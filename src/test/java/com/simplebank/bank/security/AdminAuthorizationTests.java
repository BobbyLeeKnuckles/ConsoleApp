package com.simplebank.bank.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the instructor's route-level contract through Spring's complete MVC security filter chain.
 */
@SpringBootTest(properties = "app.demo-admin.enabled=false")
@AutoConfigureMockMvc
class AdminAuthorizationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void publicEndpointDoesNotRequireAToken() throws Exception {
		mockMvc.perform(get("/public"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Public access granted. No token required."));
	}

	@Test
	void adminEndpointWithoutTokenIsForbidden() throws Exception {
		mockMvc.perform(post("/admin"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access Forbidden."));
	}

	@Test
	void adminEndpointRejectsRegularUsers() throws Exception {
		// A valid identity is not sufficient; the principal must specifically have ROLE_ADMIN.
		mockMvc.perform(post("/admin").with(user("regular-user").roles("USER")))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access Forbidden."));
	}

	@Test
	void adminEndpointAllowsAdministrators() throws Exception {
		// This request-scoped principal models the authorities established by a valid admin JWT.
		mockMvc.perform(post("/admin").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Admin access granted."))
				.andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
	}
}
