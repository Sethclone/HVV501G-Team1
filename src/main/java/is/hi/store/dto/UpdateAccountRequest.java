package is.hi.store.dto;

// UC6. No role or email field on purpose - role can't be self-promoted (decision #4) and the
// email is a work email assigned by an admin. All fields optional, only set ones are changed.
public class UpdateAccountRequest {
	private String name;
	private String password;
	private String currentPassword;	// required when password is set

	public UpdateAccountRequest() {}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public String getPassword() {return password;}
	public void setPassword(String password) {this.password = password;}
	public String getCurrentPassword() {return currentPassword;}
	public void setCurrentPassword(String currentPassword) {this.currentPassword = currentPassword;}
}
