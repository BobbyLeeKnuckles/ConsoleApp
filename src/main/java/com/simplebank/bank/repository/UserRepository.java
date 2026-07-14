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
	Optional<User> findByEmail(String email);
}
