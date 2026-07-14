package com.simplebank.bank.dto;

/**
 * JSON response returned after successful login.
 */
public record LoginResponse(String token, String userId, String name, String email) {
}
