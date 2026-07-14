package com.simplebank.bank.dto;

import java.math.BigDecimal;

/**
 * JSON request body for moving money from one account to another.
 */
public record TransferRequest(String receiverAccountId, BigDecimal amount) {
}
