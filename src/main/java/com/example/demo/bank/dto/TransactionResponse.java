package com.example.demo.bank.dto;

import com.example.demo.bank.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
		String transactionId,
		TransactionType type,
		BigDecimal amount,
		Instant date
) {
}
