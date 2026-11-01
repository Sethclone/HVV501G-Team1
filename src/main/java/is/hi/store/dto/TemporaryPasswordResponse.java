package is.hi.store.dto;

// UC7. Returned once by the reset endpoint, only the hash is stored, so it can't be fetched again.
public class TemporaryPasswordResponse {
	private String temporaryPassword;

	public TemporaryPasswordResponse() {}

	public TemporaryPasswordResponse(String temporaryPassword) {
		this.temporaryPassword = temporaryPassword;
	}

	public String getTemporaryPassword() {return temporaryPassword;}
	public void setTemporaryPassword(String temporaryPassword) {this.temporaryPassword = temporaryPassword;}
}
