package com.simplebank.bank.security;

import com.simplebank.bank.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	private final AuthService authService;

	public AuthInterceptor(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (request.getMethod().equals("OPTIONS") || isPublicEndpoint(request)) {
			return true;
		}
		String token = request.getHeader("X-Auth-Token");
		if (authService.isValidToken(token)) {
			return true;
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Login is required.\"}");
		return false;
	}

	private boolean isPublicEndpoint(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.equals("/")
				|| path.equals("/index.html")
				|| path.equals("/styles.css")
				|| path.equals("/app.js")
				|| path.startsWith("/api/auth/")
				|| (path.equals("/api/accounts") && request.getMethod().equals("POST"));
	}
}
