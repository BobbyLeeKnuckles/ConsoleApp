package com.example.demo.bank.dto;

public record CreateAccountRequest(
		String name,
		String email,
		String password,
		String accountType
) {
}
