package com.simplebank.bank.config;

import com.simplebank.bank.model.User;
import com.simplebank.bank.repository.UserRepository;
import com.simplebank.bank.security.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates a small demo admin login for classroom testing.
 *
 * This is intentionally simple: the admin screen still uses the normal JWT login endpoint.
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
			return;
		}

		String normalizedUsername = username.trim().toLowerCase();
		String passwordHash = PasswordHasher.hash(password);
		userRepository.findByEmail(normalizedUsername)
				.map(existingAdmin -> updateDemoAdmin(existingAdmin, passwordHash))
				.orElseGet(() -> userRepository.save(new User(normalizedUsername, normalizedUsername, passwordHash)));
	}

	private User updateDemoAdmin(User admin, String passwordHash) {
		// Keep the classroom admin password predictable even after database resets or edits.
		admin.updateName(username.trim());
		admin.updatePasswordHash(passwordHash);
		return userRepository.save(admin);
	}
}
