package com.simplebank.bank.config;

import com.simplebank.bank.model.User;
import com.simplebank.bank.model.UserRole;
import com.simplebank.bank.repository.UserRepository;
import com.simplebank.bank.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates a small demo admin login for classroom testing.
 *
 * This is intentionally simple: the seeded account still authenticates through the normal JWT login endpoint. The
 * seeder guarantees that classroom credentials map to ROLE_ADMIN before protected endpoint testing begins.
 */
@Component
public class DemoAdminSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final boolean enabled;
	private final String username;
	private final String password;

	public DemoAdminSeeder(
			UserRepository userRepository,
			@Value("${app.demo-admin.enabled:true}") boolean enabled,
			@Value("${app.demo-admin.username:admin}") String username,
			@Value("${app.demo-admin.password:admin123}") String password
	) {
		this.userRepository = userRepository;
		this.enabled = enabled;
		this.username = username;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (!enabled) {
			// Tests can disable database writes while still loading the complete Spring application context.
			return;
		}

		// Use the same username and password representation expected by BankUserDetailsService and PasswordEncoder.
		String normalizedUsername = username.trim().toLowerCase();
		String passwordHash = PasswordHasher.hash(password);
		userRepository.findByEmail(normalizedUsername)
				.map(existingAdmin -> updateDemoAdmin(existingAdmin, passwordHash))
				.orElseGet(() -> userRepository.save(
						new User(normalizedUsername, normalizedUsername, passwordHash, UserRole.ADMIN)
				));
	}

	private User updateDemoAdmin(User admin, String passwordHash) {
		// Keep the classroom admin password predictable even after database resets or edits.
		admin.updateName(username.trim());
		admin.updatePasswordHash(passwordHash);
		// Upgrade older demo records that existed before role-based authorization was introduced.
		admin.updateRole(UserRole.ADMIN);
		return userRepository.save(admin);
	}
}
