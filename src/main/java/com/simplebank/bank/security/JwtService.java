package com.simplebank.bank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Generates and validates stateless JSON Web Tokens (JWTs).
 *
 * The server signs each token with an HMAC secret. Later requests can be authenticated by verifying that signature
 * and the standard issuer/expiration claims, without storing a server-side login session.
 */
@Service
public class JwtService {

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);
	// HS256 requires at least 256 bits (32 bytes) of key material.
	private static final int MIN_SECRET_BYTES = 32;
	// Requiring a fixed issuer prevents tokens created for another application from being accepted here.
	private static final String ISSUER = "simple-bank-app";

	// JJWT uses this key both to sign newly issued tokens and verify incoming token signatures.
	private final SecretKey signingKey;
	private final long expirationMinutes;
	// Injecting a Clock makes expiration behavior deterministic in unit tests.
	private final Clock clock;

	/**
	 * Production constructor populated from application properties or environment variables.
	 */
	@Autowired
	public JwtService(
			@Value("${security.jwt.secret:}") String configuredSecret,
			@Value("${security.jwt.expiration-minutes:60}") long expirationMinutes
	) {
		this(configuredSecret, expirationMinutes, Clock.systemUTC());
	}

	/**
	 * Package-private constructor used by tests to control the secret, lifetime, and current time.
	 */
	JwtService(String configuredSecret, long expirationMinutes, Clock clock) {
		if (expirationMinutes <= 0) {
			throw new IllegalStateException("security.jwt.expiration-minutes must be greater than 0.");
		}
		this.signingKey = Keys.hmacShaKeyFor(secretBytes(configuredSecret));
		this.expirationMinutes = expirationMinutes;
		this.clock = clock;
	}

	/**
	 * Returns the token subject. This application uses the normalized login email/username as the subject.
	 */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	/**
	 * Reads one typed claim after the token has passed cryptographic and time validation.
	 */
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		// parseSignedClaims rejects bad signatures, the wrong issuer, malformed JWTs, and expired tokens.
		return Jwts.parser()
				.verifyWith(signingKey)
				.requireIssuer(ISSUER)
				.clock(() -> Date.from(clock.instant()))
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	/**
	 * Creates a signed JWT for an authenticated Spring Security principal.
	 *
	 * Role names are included for transparent client inspection. The request filter still reloads authorities from
	 * MongoDB, so changing a user's role takes effect without waiting for every old token to expire.
	 */
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(
				"roles",
				userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
		);
		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		Instant issuedAt = clock.instant();
		// Passwords and password hashes must never be placed in JWT claims because JWT payloads are readable.
		return Jwts.builder()
				.claims(claims)
				.issuer(ISSUER)
				.subject(subject)
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt()))
				.signWith(signingKey, Jwts.SIG.HS256)
				.compact();
	}

	/**
	 * Confirms that the token belongs to the loaded user and has not expired or failed signature validation.
	 */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			String username = extractUsername(token);
			return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
		} catch (JwtException | IllegalArgumentException exception) {
			// Security validation fails closed: any parsing or cryptographic error means the token is unusable.
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		return extractClaim(token, Claims::getExpiration).before(Date.from(clock.instant()));
	}

	public Instant expiresAt() {
		// AuthService returns this value so clients know when they must log in again.
		return clock.instant().plus(expirationMinutes, ChronoUnit.MINUTES);
	}

	private byte[] secretBytes(String configuredSecret) {
		if (configuredSecret == null || configuredSecret.isBlank()) {
			// A random startup key is convenient for development but invalidates all tokens whenever the app restarts.
			LOGGER.warn("JWT_SECRET is not set. Using a generated development-only JWT secret for this app run.");
			byte[] generatedSecret = new byte[MIN_SECRET_BYTES];
			new SecureRandom().nextBytes(generatedSecret);
			return generatedSecret;
		}

		byte[] secretBytes = decodeSecret(configuredSecret.trim());
		if (secretBytes.length < MIN_SECRET_BYTES) {
			// Refuse weak configured keys instead of silently issuing tokens with inadequate signing strength.
			throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256. Set a stronger JWT_SECRET.");
		}
		return secretBytes;
	}

	private byte[] decodeSecret(String secret) {
		if (secret.startsWith("base64:")) {
			// The prefix lets deployment systems provide binary key material as portable text.
			return Base64.getDecoder().decode(secret.substring("base64:".length()));
		}
		return secret.getBytes(StandardCharsets.UTF_8);
	}
}
