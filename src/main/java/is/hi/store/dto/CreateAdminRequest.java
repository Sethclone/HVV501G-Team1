package is.hi.store.dto;

// UC16. Same as RegisterRequest, but the contract names the field "name". Jackson fills it through this
// constructor, so no fields of its own: shadowing the parent's left email and password null.
public class CreateAdminRequest extends RegisterRequest {
	public CreateAdminRequest(String name, String email, String password) {
		super(name, email, password);
	}
}
