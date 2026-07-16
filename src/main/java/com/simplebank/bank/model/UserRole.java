package com.simplebank.bank.model;

/**
 * Roles used by Spring Security when authorizing API requests.
 */
public enum UserRole {
	/** Standard account with no access to the /admin namespace. */
	USER,
	/** Administrator account accepted by hasRole("ADMIN"). */
	ADMIN
}
