package com.simplebank.bank.repository;

import com.simplebank.bank.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
	List<Transaction> findByAccountIdOrderByCreatedAtDesc(String accountId);

	Page<Transaction> findByAccountId(String accountId, Pageable pageable);
}
