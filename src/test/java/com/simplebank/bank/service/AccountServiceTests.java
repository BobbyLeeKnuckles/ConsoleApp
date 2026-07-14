package com.simplebank.bank.service;

import com.simplebank.bank.dto.AccountResponse;
import com.simplebank.bank.model.Account;
import com.simplebank.bank.model.Transaction;
import com.simplebank.bank.model.TransactionType;
import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.AccountRepository;
import com.simplebank.bank.repository.TransactionRepository;
import com.simplebank.bank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD-style unit tests for AccountService business rules.
 *
 * These tests use mocks instead of MongoDB so they run fast and focus only on service behavior.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTests {

	private static final String ACCOUNT_ID = "account-1";
	private static final String USER_ID = "user-1";

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionRepository transactionRepository;

	private AccountService accountService;

	@BeforeEach
	void setUp() {
		// Build the service by hand so the test controls each repository dependency.
		accountService = new AccountService(userRepository, accountRepository, transactionRepository);
	}

	@Test
	void depositAddsMoneyAndRecordsDepositTransaction() {
		// Arrange: create test objects and tell mocks what to return.
		Account account = new Account(USER_ID, "SAVINGS");
		User user = new User("Hoang Huynh", "hoang@example.com", "password-hash");
		BigDecimal amount = new BigDecimal("50.00");

		when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));
		when(accountRepository.save(account)).thenReturn(account);
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		// Act: call the method being tested.
		AccountResponse response = accountService.deposit(ACCOUNT_ID, amount);

		// Assert: check the visible response and the transaction side effect.
		assertEquals(amount, response.balance());
		ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
		verify(transactionRepository).save(transactionCaptor.capture());
		Transaction transaction = transactionCaptor.getValue();
		assertEquals(ACCOUNT_ID, transaction.getAccountId());
		assertEquals(TransactionType.DEPOSIT, transaction.getType());
		assertEquals(amount, transaction.getAmount());
	}

	@Test
	void withdrawRejectsAmountGreaterThanBalance() {
		// Arrange: new accounts start with a zero balance.
		Account account = new Account(USER_ID, "CHECKING");

		when(accountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(account));

		// Act + Assert: the service should reject an overdraft attempt.
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> accountService.withdraw(ACCOUNT_ID, new BigDecimal("25.00"))
		);

		assertEquals("Cannot withdraw more than the current balance.", exception.getMessage());
		// A failed withdrawal must not save the account or create a transaction.
		verify(accountRepository, never()).save(any(Account.class));
		verify(transactionRepository, never()).save(any(Transaction.class));
	}
}
