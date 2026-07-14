package com.simplebank.bank.dto;

import java.math.BigDecimal;

/**
 * Shared JSON request body for deposit and withdraw operations.
 */
public record MoneyRequest(BigDecimal amount) {
}
