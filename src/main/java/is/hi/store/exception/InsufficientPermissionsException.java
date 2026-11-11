package is.hi.store.exception;

public class InsufficientPermissionsException extends RuntimeException {
	public InsufficientPermissionsException(String message) {
		super(message);
	}
}
