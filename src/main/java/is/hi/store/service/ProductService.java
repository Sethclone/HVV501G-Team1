package is.hi.store.service;

import is.hi.store.dto.ProductResponse;
import is.hi.store.entity.Product;
import is.hi.store.exception.ProductNotFoundException;
import is.hi.store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


public interface ProductService {
    ProductResponse getProductById(Long id);

}


@Service
class ProductServiceImplementation implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        return new ProductResponse(product);
    }
}
