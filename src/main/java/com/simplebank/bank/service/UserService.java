package com.simplebank.bank.service;

import com.simplebank.bank.dto.UserResponse;
import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business layer for admin user lookups.
 */
@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<UserResponse> findAllUsers() {
		// Sort by email so the admin table stays predictable while testing in the browser or Postman.
		return userRepository.findAll(Sort.by(Sort.Direction.ASC, "email"))
				.stream()
				.map(this::toUserResponse)
				.toList();
	}

	private UserResponse toUserResponse(User user) {
		// Return display fields only. Password hashes stay inside the User document.
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}
}
