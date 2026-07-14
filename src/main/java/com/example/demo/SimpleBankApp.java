package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class SimpleBankApp {

	public static void main(String[] args) {
		SpringApplication.run(SimpleBankApp.class, args);
	}

}
