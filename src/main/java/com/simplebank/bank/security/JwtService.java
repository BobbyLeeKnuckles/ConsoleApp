package com.simplebank.bank.security;

import com.simplebank.bank.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * Creates and verifies JWTs for the Simple Bank API.
 *
 * A JWT is stateless: the server validates the signature and expiration instead of storing tokens in memory.
 */
@Service
public class JwtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);
	private static final int MIN_SECRET_BYTES = 32;
	private static final String ISSUER = "simple-bank-app";

	private final SecretKey signingKey;
	private final long expirationMinutes;
	private final Clock clock;

	@Autowired
	public JwtService(
			@Value("${security.jwt.secret:}") String configuredSecret,
			@Value("${security.jwt.expiration-minutes:60}") long expirationMinutes
	) {
		this(configuredSecret, expirationMinutes, Clock.systemUTC());
	}

	JwtService(String configuredSecret, long expirationMinutes, Clock clock) {
		if (expirationMinutes <= 0) {
			throw new IllegalStateException("security.jwt.expiration-minutes must be greater than 0.");
		}
		this.signingKey = Keys.hmacShaKeyFor(secretBytes(configuredSecret));
		this.expirationMinutes = expirationMinutes;
		this.clock = clock;
	}

	public String createToken(User user) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = expiresAt();
		// Store only non-sensitive user facts in the token. Never place passwords or password hashes in JWT claims.
		return Jwts.builder()
				.issuer(ISSUER)
				.subject(user.getId())
				.claim("name", user.getName())
				.claim("email", user.getEmail())
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey, Jwts.SIG.HS256)
				.compact();
	}

	public boolean isValidToken(String token) {
		return parseClaims(token).isPresent();
	}

	public Instant expiresAt() {
		return clock.instant().plus(expirationMinutes, ChronoUnit.MINUTES);
	}

	Optional<String> userIdFromToken(String token) {
		return parseClaims(token).map(Claims::getSubject);
	}

	private Optional<Claims> parseClaims(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		try {
			Claims claims = Jwts.parser()
					.verifyWith(signingKey)
					.requireIssuer(ISSUER)
					.clock(() -> Date.from(clock.instant()))
					.build()
					.parseSignedClaims(token)
					.getPayload();
			return Optional.of(claims);
		} catch (JwtException | IllegalArgumentException exception) {
			// Invalid, expired, or tampered tokens all fail closed.
			return Optional.empty();
		}
	}

	private byte[] secretBytes(String configuredSecret) {
		if (configuredSecret == null || configuredSecret.isBlank()) {
			LOGGER.warn("JWT_SECRET is not set. Using a generated development-only JWT secret for this app run.");
			byte[] generatedSecret = new byte[MIN_SECRET_BYTES];
			new SecureRandom().nextBytes(generatedSecret);
			return generatedSecret;
		}

		byte[] secretBytes = decodeSecret(configuredSecret.trim());
		if (secretBytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256. Set a stronger JWT_SECRET.");
		}
		return secretBytes;
	}

	private byte[] decodeSecret(String secret) {
		if (secret.startsWith("base64:")) {
			return Base64.getDecoder().decode(secret.substring("base64:".length()));
		}
		return secret.getBytes(StandardCharsets.UTF_8);
	}
}
