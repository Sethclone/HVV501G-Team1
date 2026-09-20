package is.hi.store.controller;


import is.hi.store.dto.ProductResponse;
import is.hi.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.dto.ProductCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@ModelAttribute ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
