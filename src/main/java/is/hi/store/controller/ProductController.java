package is.hi.store.controller;

import is.hi.store.dto.ProductResponse;
import is.hi.store.dto.ReorderRequest;
import is.hi.store.dto.StockMovementRequest;
import is.hi.store.dto.StockMovementResponse;
import is.hi.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.dto.ProductCreateRequest;
import org.springframework.security.core.Authentication;
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

	@PostMapping("/{productId}/stock-movements")
	public ResponseEntity<StockMovementResponse> stockMovement(
		Authentication auth,
		@PathVariable Long productId,
		@RequestBody StockMovementRequest request
	) {
		StockMovementResponse response = productService.stockMovement(productId, auth.getName(), request);	
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/{id}/reorder-flag")
	public ResponseEntity<ProductResponse> flagProduct(
		@PathVariable Long id,
		@RequestBody ReorderRequest request
	) {
		ProductResponse response = productService.flagProduct(id, request.getFlagged());
		return ResponseEntity.ok(response);
	}
}
