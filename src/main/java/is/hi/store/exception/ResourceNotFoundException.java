package is.hi.store.exception;

//Just because it's annoying to create a bunch of essentially
//the same exception classes for different resources.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName,Long id) {
        super(resourceName + " not found with id: " + id);
    }
}
