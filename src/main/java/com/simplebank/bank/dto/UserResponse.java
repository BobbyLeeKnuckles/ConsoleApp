package com.simplebank.bank.dto;

import java.time.Instant;

/**
 * JSON response for admin user lists.
 *
 * This DTO intentionally leaves out passwordHash so credentials never leave the backend.
 */
public record UserResponse(String userId, String name, String email, Instant createdAt) {
}
