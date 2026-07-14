package com.simplebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class SimpleBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleBankApplication.class, args);
	}

}
