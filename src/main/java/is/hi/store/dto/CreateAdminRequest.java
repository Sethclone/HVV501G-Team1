package is.hi.store.dto;

import is.hi.store.dto.RegisterRequest;
public class CreateAdminRequest extends RegisterRequest{
	private String name;
	private String email;
	private String password;

	public CreateAdminRequest(String name, String email, String password) {
		super(name, email, password);
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
}
