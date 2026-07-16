package com.simplebank.bank.dto;

import com.simplebank.bank.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JSON response row for the transaction history table.
 */
public record TransactionResponse(
		String transactionId,
		TransactionType type,
		BigDecimal amount,
		Instant date
) {
	// This DTO is intentionally table-shaped for the React transaction history page.
}
