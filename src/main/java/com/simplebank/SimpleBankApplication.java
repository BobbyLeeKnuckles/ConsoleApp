package com.simplebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Main entry point for the Spring Boot application.
 *
 * {@code @SpringBootApplication} starts component scanning, auto-configuration, and the embedded web server.
 * {@code @EnableMongoAuditing} lets MongoDB fill fields marked with {@code @CreatedDate}.
 */
@EnableMongoAuditing
@SpringBootApplication
public class SimpleBankApplication {

	public static void main(String[] args) {
		// Starts the HTTP server and loads controllers, services, repositories, and configuration classes.
		SpringApplication.run(SimpleBankApplication.class, args);
	}

}
