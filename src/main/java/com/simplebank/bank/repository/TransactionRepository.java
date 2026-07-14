package com.simplebank.bank.repository;

import com.simplebank.bank.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Data-access layer for transactions.
 *
 * The custom query methods support both full history and paginated history views.
 */
public interface TransactionRepository extends MongoRepository<Transaction, String> {
	List<Transaction> findByAccountIdOrderByCreatedAtDesc(String accountId);

	Page<Transaction> findByAccountId(String accountId, Pageable pageable);
}
