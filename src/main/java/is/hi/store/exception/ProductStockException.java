package is.hi.store.exception;

public class ProductStockException extends RuntimeException {
    public ProductStockException(Long id){
        super("Product stock is too low for this operation: " + id);
    }
}
