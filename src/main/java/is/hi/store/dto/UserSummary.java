package is.hi.store.dto;

import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;

public class UserSummary {
	private long id;
	private String name;
	private String email;
	private Role role;
	private String profilePicURL;

	public UserSummary() {}

	public UserSummary(User user) {
		this.id = user.getId();
		this.name = user.getUsername();
		this.email = user.getEmail();
		this.role = user.getRole();
		this.profilePicURL = user.getProfilePictureURL();
	}

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public Role getRole() { return role; }
	public void setRole(Role role) { this.role = role; }
	
	public String getProfilePicURL() { return profilePicURL; }
	public void setProfilePicURL(String profilePicURL) { this.profilePicURL = profilePicURL; }
}
