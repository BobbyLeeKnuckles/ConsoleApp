package com.simplebank.bank.dto;

import java.time.Instant;

/**
 * Standard JSON shape for errors returned by ApiExceptionHandler.
 */
public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message
) {
}
