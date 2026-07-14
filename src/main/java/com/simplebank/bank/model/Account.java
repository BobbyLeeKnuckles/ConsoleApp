package com.simplebank.bank.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB document for a bank account.
 *
 * The account stores the balance and points back to the user who owns it.
 */
@Document("accounts")
public class Account {

	@Id
	private String id;

	private String userId;
	// BigDecimal is preferred for money because it avoids floating-point rounding issues.
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
		// The service validates the amount before calling this model method.
		this.balance = this.balance.add(amount);
	}

	public void withdraw(BigDecimal amount) {
		// Overdraft checks live in the service because they depend on business rules.
		this.balance = this.balance.subtract(amount);
	}
}
