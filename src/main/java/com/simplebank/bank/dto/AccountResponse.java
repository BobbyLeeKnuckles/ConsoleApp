package com.simplebank.bank.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JSON response returned when the API sends account details to the client.
 */
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
