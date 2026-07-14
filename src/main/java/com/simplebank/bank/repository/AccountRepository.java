package com.simplebank.bank.repository;

import com.simplebank.bank.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Data-access layer for accounts.
 *
 * MongoRepository gives this interface CRUD methods such as findById, findAll, save, and delete.
 */
public interface AccountRepository extends MongoRepository<Account, String> {
}
