package com.simplebank.bank.repository;

import com.simplebank.bank.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Data-access layer for users.
 *
 * Spring Data builds the findByEmail query from the method name.
 */
public interface UserRepository extends MongoRepository<User, String> {
	// Used by registration to prevent duplicate users and by login to find credentials.
	Optional<User> findByEmail(String email);
}
