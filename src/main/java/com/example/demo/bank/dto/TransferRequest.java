package com.example.demo.bank.dto;

import java.math.BigDecimal;

public record TransferRequest(String receiverAccountId, BigDecimal amount) {
}
