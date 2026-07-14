package com.simplebank.bank.service;

import com.simplebank.bank.dto.AccountResponse;
import com.simplebank.bank.dto.CreateAccountRequest;
import com.simplebank.bank.dto.TransactionResponse;
import com.simplebank.bank.dto.TransferRequest;
import com.simplebank.bank.dto.UpdateUserRequest;
import com.simplebank.bank.model.Account;
import com.simplebank.bank.model.AccountType;
import com.simplebank.bank.model.Transaction;
import com.simplebank.bank.model.TransactionType;
import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.AccountRepository;
import com.simplebank.bank.repository.TransactionRepository;
import com.simplebank.bank.repository.UserRepository;
import com.simplebank.bank.security.PasswordHasher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AccountService {

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public AccountService(
			UserRepository userRepository,
			AccountRepository accountRepository,
			TransactionRepository transactionRepository
	) {
		this.userRepository = userRepository;
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	public List<AccountResponse> findAll() {
		return accountRepository.findAll()
				.stream()
				.map(account -> toAccountResponse(account, findUser(account.getUserId())))
				.toList();
	}

	public AccountResponse createAccount(CreateAccountRequest request) {
		validateCreateAccountRequest(request);
		String email = request.email().trim().toLowerCase();
		String name = request.name().trim();
		AccountType accountType = parseAccountType(request.accountType());
		String passwordHash = PasswordHasher.hash(request.password());
		User user = userRepository.findByEmail(email)
				.map(existingUser -> prepareExistingUser(existingUser, passwordHash))
				.orElseGet(() -> userRepository.save(new User(name, email, passwordHash)));
		Account account = accountRepository.save(new Account(user.getId(), accountType.name()));
		return toAccountResponse(account, user);
	}

	public AccountResponse getAccount(String accountId) {
		Account account = findAccount(accountId);
		User user = findUser(account.getUserId());
		return toAccountResponse(account, user);
	}

	public AccountResponse updateAccountUser(String accountId, UpdateUserRequest request) {
		if (request.name() == null || request.name().isBlank()) {
			throw new IllegalArgumentException("Name cannot be empty.");
		}
		Account account = findAccount(accountId);
		User user = findUser(account.getUserId());
		user.updateName(request.name().trim());
		User savedUser = userRepository.save(user);
		return toAccountResponse(account, savedUser);
	}

	public AccountResponse deposit(String accountId, BigDecimal amount) {
		validatePositiveAmount(amount);
		Account account = findAccount(accountId);
		account.deposit(amount);
		Account saved = accountRepository.save(account);
		transactionRepository.save(new Transaction(accountId, TransactionType.DEPOSIT, amount));
		return toAccountResponse(saved, findUser(saved.getUserId()));
	}

	public AccountResponse withdraw(String accountId, BigDecimal amount) {
		validatePositiveAmount(amount);
		Account account = findAccount(accountId);
		if (account.getBalance().compareTo(amount) < 0) {
			throw new IllegalArgumentException("Cannot withdraw more than the current balance.");
		}
		account.withdraw(amount);
		Account saved = accountRepository.save(account);
		transactionRepository.save(new Transaction(accountId, TransactionType.WITHDRAW, amount));
		return toAccountResponse(saved, findUser(saved.getUserId()));
	}

	public AccountResponse transfer(String senderAccountId, TransferRequest request) {
		if (request == null || request.receiverAccountId() == null || request.receiverAccountId().isBlank()) {
			throw new IllegalArgumentException("Receiver account ID is required.");
		}
		validatePositiveAmount(request.amount());
		if (senderAccountId.equals(request.receiverAccountId())) {
			throw new IllegalArgumentException("Cannot transfer to the same account.");
		}
		Account sender = findAccount(senderAccountId);
		Account receiver = findAccount(request.receiverAccountId());
		if (sender.getBalance().compareTo(request.amount()) < 0) {
			throw new IllegalArgumentException("Cannot transfer more than the current balance.");
		}
		sender.withdraw(request.amount());
		receiver.deposit(request.amount());
		Account savedSender = accountRepository.save(sender);
		accountRepository.save(receiver);
		transactionRepository.save(new Transaction(senderAccountId, TransactionType.TRANSFER_OUT, request.amount()));
		transactionRepository.save(new Transaction(receiver.getId(), TransactionType.TRANSFER_IN, request.amount()));
		return toAccountResponse(savedSender, findUser(savedSender.getUserId()));
	}

	public List<TransactionResponse> getTransactions(String accountId) {
		findAccount(accountId);
		return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
				.stream()
				.map(transaction -> new TransactionResponse(
						transaction.getId(),
						transaction.getType(),
						transaction.getAmount(),
						transaction.getCreatedAt()
				))
				.toList();
	}

	public Page<TransactionResponse> getTransactions(String accountId, int page, int size) {
		findAccount(accountId);
		int safePage = Math.max(page, 0);
		int safeSize = Math.min(Math.max(size, 1), 25);
		return transactionRepository.findByAccountId(
						accountId,
						PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
				)
				.map(transaction -> new TransactionResponse(
						transaction.getId(),
						transaction.getType(),
						transaction.getAmount(),
						transaction.getCreatedAt()
				));
	}

	private Account findAccount(String accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new NoSuchElementException("Account not found: " + accountId));
	}

	private User findUser(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
	}

	private void validatePositiveAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be positive.");
		}
	}

	private User prepareExistingUser(User user, String passwordHash) {
		if (user.getPasswordHash() == null) {
			user.updatePasswordHash(passwordHash);
			return userRepository.save(user);
		}
		if (!user.getPasswordHash().equals(passwordHash)) {
			throw new IllegalArgumentException("Email already exists. Login with the original password.");
		}
		return user;
	}

	private void validateCreateAccountRequest(CreateAccountRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request body is required.");
		}
		if (request.name() == null || request.name().isBlank()) {
			throw new IllegalArgumentException("Name cannot be empty.");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("Email cannot be empty.");
		}
		if (!request.email().contains("@")) {
			throw new IllegalArgumentException("Email must be valid.");
		}
		if (request.password() == null || request.password().length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters.");
		}
	}

	private AccountType parseAccountType(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Account type cannot be empty.");
		}
		String normalized = value.trim().toUpperCase();
		if (normalized.equals("CHECKINGS")) {
			normalized = "CHECKING";
		}
		try {
			return AccountType.valueOf(normalized);
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Account type must be CHECKING or SAVINGS.");
		}
	}

	private AccountResponse toAccountResponse(Account account, User user) {
		return new AccountResponse(
				account.getId(),
				user.getId(),
				user.getName(),
				user.getEmail(),
				account.getAccountType(),
				account.getBalance(),
				account.getCreatedAt()
		);
	}
}
