package com.example.demo.bank.controller;

import com.example.demo.bank.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException exception) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler({
			IllegalArgumentException.class,
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
		return buildError(HttpStatus.BAD_REQUEST, readableMessage(exception));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResource() {
		return buildError(HttpStatus.NOT_FOUND, "Endpoint not found.");
	}

	private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
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
