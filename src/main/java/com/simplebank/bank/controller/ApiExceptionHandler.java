package com.simplebank.bank.controller;

import com.simplebank.bank.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * Converts Java exceptions into consistent JSON error responses.
 *
 * Without this class, Spring would return longer default error pages/responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException exception) {
		// Missing accounts/users should be a 404 instead of a generic server error.
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler({
			IllegalArgumentException.class,
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
		// Validation failures and bad JSON are client mistakes, so they become HTTP 400.
		return buildError(HttpStatus.BAD_REQUEST, readableMessage(exception));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResource() {
		// Handles unknown URLs such as /api/does-not-exist.
		return buildError(HttpStatus.NOT_FOUND, "Endpoint not found.");
	}

	private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
		// ErrorResponse is the shared shape returned to Postman and the browser UI.
		return ResponseEntity.status(status)
				.body(new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message));
	}

	private String readableMessage(Exception exception) {
		if (exception instanceof HttpMessageNotReadableException) {
			return "Request body must be valid JSON.";
		}
		return exception.getMessage();
	}
}
