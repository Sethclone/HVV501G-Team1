package is.hi.store.dto;

import java.time.Instant;
import is.hi.store.entity.User.Role;
import is.hi.store.entity.User;
public class RegisterResponse {
	private long id;
	private String username;
	private String email;
	private Role role;
	private Instant createdAt;

	public RegisterResponse(User user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.email = user.getEmail();
		this.role = user.getRole();
		this.createdAt = user.getCreatedAt();
	}

	public long getId() {return id;}
	public String getUsername() {return username;}
	public String getEmail() {return email;}
	public Role getRole() {return role;}
	public Instant getCreatedAt() {return createdAt;}
}
