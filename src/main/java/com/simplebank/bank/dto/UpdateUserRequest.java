package com.simplebank.bank.dto;

/**
 * JSON request body for changing the account holder's display name.
 */
public record UpdateUserRequest(String name) {
}
