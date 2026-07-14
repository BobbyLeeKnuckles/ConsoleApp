package com.simplebank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies that the Spring application context can start.
 *
 * This catches wiring problems such as missing beans, bad package names, or broken configuration.
 */
@SpringBootTest
class SimpleBankApplicationTests {

	@Test
	void contextLoads() {
		// No assertions needed. If Spring cannot start, this test fails automatically.
	}

}
