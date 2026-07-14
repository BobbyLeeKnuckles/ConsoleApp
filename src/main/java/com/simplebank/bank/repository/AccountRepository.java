package com.simplebank.bank.repository;

import com.simplebank.bank.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
}
