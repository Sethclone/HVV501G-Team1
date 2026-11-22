package is.hi.store.exception;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String fileName) {
        super(fileName + " not found");
    }
}
