package com.simplebank.bank.service;

import com.simplebank.bank.model.SequenceCounter;
import com.simplebank.bank.model.Account;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Generates account IDs like SB-1001, SB-1002, and SB-1003.
 *
 * MongoDB increments the counter atomically so two requests do not receive the same account number.
 */
@Service
public class AccountNumberService {

	private static final String ACCOUNT_COUNTER_ID = "account";
	private static final long ACCOUNT_NUMBER_OFFSET = 1000;
	private static final String ACCOUNT_NUMBER_PREFIX = "SB-";

	private final MongoTemplate mongoTemplate;

	public AccountNumberService(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public String nextAccountId() {
		String accountId;
		do {
			accountId = ACCOUNT_NUMBER_PREFIX + (ACCOUNT_NUMBER_OFFSET + nextSequence());
		} while (accountExists(accountId));
		return accountId;
	}

	private long nextSequence() {
		Query query = Query.query(Criteria.where("_id").is(ACCOUNT_COUNTER_ID));
		Update update = new Update().inc("sequence", 1);
		FindAndModifyOptions options = FindAndModifyOptions.options()
				.upsert(true)
				.returnNew(true);
		SequenceCounter counter = mongoTemplate.findAndModify(query, update, options, SequenceCounter.class);
		if (counter == null) {
			throw new IllegalStateException("Could not generate account number.");
		}
		return counter.getSequence();
	}

	private boolean accountExists(String accountId) {
		// If the database already has this id, skip it and move to the next readable number.
		return mongoTemplate.exists(Query.query(Criteria.where("_id").is(accountId)), Account.class);
	}
}
