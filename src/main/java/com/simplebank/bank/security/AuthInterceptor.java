package com.simplebank.bank.security;

import com.simplebank.bank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Lightweight authentication gate for API routes.
 *
 * Spring MVC calls preHandle before the controller method runs.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

	private final AuthService authService;

	public AuthInterceptor(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// Browser preflight requests and public routes do not need a login token.
		if (request.getMethod().equals("OPTIONS") || isPublicEndpoint(request)) {
			return true;
		}
		// Protected requests must include the token returned from /api/auth/login.
		String token = request.getHeader("X-Auth-Token");
		if (authService.isValidToken(token)) {
			return true;
		}
		// Returning false stops Spring from calling the controller.
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Login is required.\"}");
		return false;
	}

	private boolean isPublicEndpoint(HttpServletRequest request) {
		String path = request.getRequestURI();
		// Static files and account creation must stay public so users can open the UI and register.
		return path.equals("/")
				|| path.equals("/index.html")
				|| path.equals("/styles.css")
				|| path.equals("/app.js")
				|| path.startsWith("/api/auth/")
				|| (path.equals("/api/accounts") && request.getMethod().equals("POST"));
	}
}
