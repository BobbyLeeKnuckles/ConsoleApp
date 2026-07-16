package com.simplebank.bank.config;

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

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Allows the Vite dev server to call the Spring Boot API during frontend development.
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type", "X-Auth-Token")
				.maxAge(3600);
		registry.addMapping("/admin/**")
				.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
				.allowedMethods("GET", "POST", "OPTIONS")
				.allowedHeaders("Authorization", "Content-Type", "X-Auth-Token")
				.maxAge(3600);
	}
}
