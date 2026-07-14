package com.simplebank.bank.dto;

/**
 * JSON request body for login.
 */
public record LoginRequest(String email, String password) {
}
