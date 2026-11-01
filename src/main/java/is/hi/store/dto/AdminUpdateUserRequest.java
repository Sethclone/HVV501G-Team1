package is.hi.store.dto;

import is.hi.store.entity.User.Role;

// UC7. All fields optional, only set ones are changed. Password resets go through their own
// endpoint (POST /api/admin/users/{id}/reset-password) so a temp password never lands here.
public class AdminUpdateUserRequest {
	private String name;
	private String email;
	private Role role;

	public AdminUpdateUserRequest() {}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public String getEmail() {return email;}
	public void setEmail(String email) {this.email = email;}
	public Role getRole() {return role;}
	public void setRole(Role role) {this.role = role;}
}
