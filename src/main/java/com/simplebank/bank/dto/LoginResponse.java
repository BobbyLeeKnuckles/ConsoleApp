package com.simplebank.bank.dto;

public record LoginResponse(String token, String userId, String name, String email) {
}
