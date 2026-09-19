package is.hi.store.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import is.hi.store.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

	private final SecretKey signingKey;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMs = expirationMs;
	}

	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiry = now.plusMillis(expirationMs);
		return Jwts.builder()
			.subject(user.getEmail())
			.claim("id", user.getId())
			.claim("role", user.getRole().name())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiry))
			.signWith(signingKey)
			.compact();
	}

	public Instant extractExpiration(String token) {
		return parseClaims(token).getExpiration().toInstant();
	}

	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return parseClaims(token).get("role", String.class);
	}

	public boolean isValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(signingKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
