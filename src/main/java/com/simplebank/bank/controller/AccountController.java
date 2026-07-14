package com.simplebank.bank.controller;

import com.simplebank.bank.dto.AccountResponse;
import com.simplebank.bank.dto.CreateAccountRequest;
import com.simplebank.bank.dto.MoneyRequest;
import com.simplebank.bank.dto.TransactionResponse;
import com.simplebank.bank.dto.TransferRequest;
import com.simplebank.bank.dto.UpdateUserRequest;
import com.simplebank.bank.service.AccountService;
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

/**
 * REST controller for bank account actions.
 *
 * Controllers should stay thin: they receive HTTP requests and delegate business rules to services.
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public List<AccountResponse> findAll() {
		// Returns a lightweight list for the dashboard account picker.
		return accountService.findAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
		// Account creation is public so a brand-new user can register before logging in.
		return accountService.createAccount(request);
	}

	@GetMapping("/{id}")
	public AccountResponse getAccount(@PathVariable String id) {
		// Path variables come from the URL, for example /api/accounts/123.
		return accountService.getAccount(id);
	}

	@PutMapping("/{id}/user")
	public AccountResponse updateAccountUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
		return accountService.updateAccountUser(id, request);
	}

	@PostMapping("/{id}/deposit")
	public AccountResponse deposit(@PathVariable String id, @RequestBody MoneyRequest request) {
		// The service validates the amount and records the deposit transaction.
		return accountService.deposit(id, request.amount());
	}

	@PostMapping("/{id}/withdraw")
	public AccountResponse withdraw(@PathVariable String id, @RequestBody MoneyRequest request) {
		// The service enforces the "no overdraft" rule before changing the balance.
		return accountService.withdraw(id, request.amount());
	}

	@PostMapping("/{id}/transfer")
	public AccountResponse transfer(@PathVariable String id, @RequestBody TransferRequest request) {
		// Transfers update two accounts and create matching transaction records.
		return accountService.transfer(id, request);
	}

	@GetMapping("/{id}/transactions")
	public Page<TransactionResponse> getTransactions(
			@PathVariable String id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size
	) {
		// Pagination keeps the response small when an account has many transactions.
		return accountService.getTransactions(id, page, size);
	}
}
