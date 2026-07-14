package com.simplebank.bank.dto;

import java.math.BigDecimal;

public record TransferRequest(String receiverAccountId, BigDecimal amount) {
}
