package com.example.demo.bank.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("transactions")
@CompoundIndex(name = "account_created_idx", def = "{'accountId': 1, 'createdAt': -1}")
public class Transaction {

	@Id
	private String id;

	private String accountId;
	private TransactionType type;
	private BigDecimal amount;

	@CreatedDate
	private Instant createdAt;

	public Transaction() {
	}

	public Transaction(String accountId, TransactionType type, BigDecimal amount) {
		this.accountId = accountId;
		this.type = type;
		this.amount = amount;
	}

	public String getId() {
		return id;
	}

	public String getAccountId() {
		return accountId;
	}

	public TransactionType getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
