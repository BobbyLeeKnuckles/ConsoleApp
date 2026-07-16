package com.simplebank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies that the Spring application context can start.
 *
 * This catches wiring problems such as missing beans, bad package names, or broken configuration.
 */
// The smoke test checks Spring wiring, not a live database; disabling the demo seeder keeps it deterministic.
@SpringBootTest(properties = "app.demo-admin.enabled=false")
class SimpleBankApplicationTests {

	@Test
	void contextLoads() {
		// No assertions needed. If Spring cannot start, this test fails automatically.
	}

}
