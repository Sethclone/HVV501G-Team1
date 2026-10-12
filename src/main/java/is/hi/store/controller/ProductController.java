package is.hi.store.controller;

import is.hi.store.dto.*;
import is.hi.store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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

	@GetMapping
	public ResponseEntity<PagedResponse<ProductResponse>> searchProducts(@RequestParam(required = false) String category, @RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
		return ResponseEntity.ok(productService.searchProducts(category, minPrice, maxPrice, page, size));
	}

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
        @PathVariable Long id,
        @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
