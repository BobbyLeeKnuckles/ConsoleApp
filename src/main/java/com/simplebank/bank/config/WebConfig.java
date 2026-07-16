package com.simplebank.bank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central MVC configuration.
 *
 * This is where custom web behavior is plugged into Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	private final String frontendOrigin;

	public WebConfig(@Value("${FRONTEND_ORIGIN:http://localhost:5173}") String frontendOrigin) {
		// Render will receive the Amplify URL through FRONTEND_ORIGIN; local Vite remains the default.
		this.frontendOrigin = frontendOrigin;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Allows both local Vite and the configured production frontend to call the REST API.
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", frontendOrigin)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type", "X-Auth-Token")
				.maxAge(3600);
		registry.addMapping("/admin/**")
				.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173", frontendOrigin)
				.allowedMethods("GET", "POST", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type", "X-Auth-Token")
				.maxAge(3600);
	}
}
