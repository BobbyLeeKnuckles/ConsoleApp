package com.simplebank.bank.security;

import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads bank users from MongoDB in the UserDetails format expected by Spring Security.
 *
 * The database model remains separate from Spring Security's model. This adapter exposes only the username,
 * password hash, and current role needed for authentication and authorization.
 */
@Service
public class BankUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public BankUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Finds a user by normalized email/username and converts the stored role into a GrantedAuthority.
	 *
	 * @throws UsernameNotFoundException when no account exists; the authentication provider converts this into a
	 * generic bad-credentials result so callers are not told whether a username exists.
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// Login stores usernames in lowercase, so lookup must use the same normalization rule.
		String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
		User user = userRepository.findByEmail(normalizedUsername)
				.orElseThrow(() -> new UsernameNotFoundException("User not found."));

		// roles("ADMIN") creates the authority ROLE_ADMIN required by SecurityConfiguration.
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
				.roles(user.getRole().name())
				.build();
	}
}
