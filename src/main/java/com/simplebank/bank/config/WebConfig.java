package com.simplebank.bank.config;

import com.simplebank.bank.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central MVC configuration.
 *
 * This is where custom web behavior is plugged into Spring MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;

	public WebConfig(AuthInterceptor authInterceptor) {
		this.authInterceptor = authInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// Run the auth check before every API request. Static files are left alone.
		registry.addInterceptor(authInterceptor)
				.addPathPatterns("/api/**");
	}
}
