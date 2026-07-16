package com.simplebank.bank.dto;

import java.math.BigDecimal;

/**
 * Shared JSON request body for deposit and withdraw operations.
 */
public record MoneyRequest(BigDecimal amount) {
	// BigDecimal keeps money values precise when Spring converts JSON numbers.
}
