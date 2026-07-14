package com.simplebank.bank.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
		String accountId,
		String userId,
		String userName,
		String email,
		String accountType,
		BigDecimal balance,
		Instant createdAt
) {
}
