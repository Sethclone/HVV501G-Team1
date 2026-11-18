package is.hi.store.exception;

public class UserHasOrdersException extends RuntimeException {
	public UserHasOrdersException(long id) {
		super("User " + id + " still owns orders, reassign them first");
	}
}
