package is.hi.store.dto;

import java.time.Instant;

public class LoginResponse {
	private String token;
	private Instant expiresAt;
	private UserSummary user;

	public LoginResponse() {}

	public LoginResponse(String token, Instant expiresAt, UserSummary user) {
		this.token = token;
		this.expiresAt = expiresAt;
		this.user = user;
	}

	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }

	public Instant getExpiresAt() { return expiresAt; }
	public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

	public UserSummary getUser() { return user; }
	public void setUser(UserSummary user) { this.user = user; }
}
