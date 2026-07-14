package com.simplebank.bank.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document("accounts")
public class Account {

	@Id
	private String id;

	private String userId;
	private BigDecimal balance = BigDecimal.ZERO;
	private String accountType;

	@CreatedDate
	private Instant createdAt;

	public Account() {
	}

	public Account(String userId, String accountType) {
		this.userId = userId;
		this.accountType = accountType;
	}

	public String getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public String getAccountType() {
		return accountType;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void deposit(BigDecimal amount) {
		this.balance = this.balance.add(amount);
	}

	public void withdraw(BigDecimal amount) {
		this.balance = this.balance.subtract(amount);
	}
}
