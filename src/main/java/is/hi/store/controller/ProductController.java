package is.hi.store.controller;

import is.hi.store.dto.ProductResponse;
import is.hi.store.dto.StockMovementRequest;
import is.hi.store.dto.StockMovementResponse;
import is.hi.store.service.ProductService;
import is.hi.store.config.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.dto.ProductCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
	private final JwtService jwtService;

    public ProductController(ProductService productService, JwtService jwtService) {
        this.productService = productService;
		this.jwtService = jwtService;
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

	@PostMapping("/{productId}/stock-movements")
	public ResponseEntity<StockMovementResponse> stockMovement(
		@PathVariable Long productId,
		@RequestHeader("Authorization") String token,
		@RequestBody StockMovementRequest request) {

		int userId = jwtService.extractUserId(token.substring(7));
		StockMovementResponse response = productService.stockMovement(productId, userId, request);	
		return ResponseEntity.ok(response);
	}
}
