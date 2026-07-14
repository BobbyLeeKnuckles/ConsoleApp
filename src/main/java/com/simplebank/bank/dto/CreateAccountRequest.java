package com.simplebank.bank.dto;

/**
 * JSON request body for creating a new user/account pair.
 */
public record CreateAccountRequest(
		String name,
		String email,
		String password,
		String accountType
) {
}
