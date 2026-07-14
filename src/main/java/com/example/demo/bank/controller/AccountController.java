package com.example.demo.bank.controller;

import com.example.demo.bank.dto.AccountResponse;
import com.example.demo.bank.dto.CreateAccountRequest;
import com.example.demo.bank.dto.MoneyRequest;
import com.example.demo.bank.dto.TransactionResponse;
import com.example.demo.bank.dto.TransferRequest;
import com.example.demo.bank.dto.UpdateUserRequest;
import com.example.demo.bank.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public List<AccountResponse> findAll() {
		return accountService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
		return accountService.createAccount(request);
	}

	@GetMapping("/{id}")
	public AccountResponse getAccount(@PathVariable String id) {
		return accountService.getAccount(id);
	}

	@PutMapping("/{id}/user")
	public AccountResponse updateAccountUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
		return accountService.updateAccountUser(id, request);
	}

	@PostMapping("/{id}/deposit")
	public AccountResponse deposit(@PathVariable String id, @RequestBody MoneyRequest request) {
		return accountService.deposit(id, request.amount());
	}

	@PostMapping("/{id}/withdraw")
	public AccountResponse withdraw(@PathVariable String id, @RequestBody MoneyRequest request) {
		return accountService.withdraw(id, request.amount());
	}

	@PostMapping("/{id}/transfer")
	public AccountResponse transfer(@PathVariable String id, @RequestBody TransferRequest request) {
		return accountService.transfer(id, request);
	}

	@GetMapping("/{id}/transactions")
	public Page<TransactionResponse> getTransactions(
			@PathVariable String id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size
	) {
		return accountService.getTransactions(id, page, size);
	}
}
