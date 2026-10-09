package is.hi.store.exception;

import is.hi.store.dto.FieldErrorDetail;
import java.util.List;

// 400 with fieldErrors. shared, anyone doing manual validation can throw this.
public class InvalidRequestException extends RuntimeException {
	private final List<FieldErrorDetail> fieldErrors;

	public InvalidRequestException(List<FieldErrorDetail> fieldErrors) {
		super("Validation failed");
		this.fieldErrors = fieldErrors;
	}

	public List<FieldErrorDetail> getFieldErrors() {return fieldErrors;}
}
