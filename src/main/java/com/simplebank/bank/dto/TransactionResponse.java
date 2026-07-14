package com.simplebank.bank.dto;

import com.simplebank.bank.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
		String transactionId,
		TransactionType type,
		BigDecimal amount,
		Instant date
) {
}
