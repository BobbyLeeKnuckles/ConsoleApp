package com.simplebank.bank.controller;

import com.simplebank.bank.dto.UserResponse;
import com.simplebank.bank.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for admin user actions.
 *
 * SecurityConfiguration requires an authenticated user with ROLE_ADMIN for this endpoint.
 */
@RestController
@RequestMapping("/admin/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> findAllUsers() {
		// Used by the React Admin page's "Fetch All Users" button.
		return userService.findAllUsers();
	}
}
