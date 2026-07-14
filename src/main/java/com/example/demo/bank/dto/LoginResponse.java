package com.example.demo.bank.dto;

public record LoginResponse(String token, String userId, String name, String email) {
}
