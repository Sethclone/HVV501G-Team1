package is.hi.store.exception;

public class ProductIsReferencedException extends RuntimeException {
	public ProductIsReferencedException(long id) {
		super("Product "+id+" is referenced by one or more resources; delete them first");
	}
}
