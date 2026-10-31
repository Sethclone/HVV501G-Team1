package is.hi.store.controller;

import is.hi.store.dto.CreateOrderRequest;
import is.hi.store.dto.OrderResponse;
import is.hi.store.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        Authentication auth,
        @RequestBody CreateOrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(auth.getName(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

	@DeleteMapping("/{orderId}/items/{itemId}")
	public ResponseEntity<Void> removeItem(
		@PathVariable Long orderId,
		@PathVariable Long itemId
	) {
		orderService.removeItem(orderId, itemId);
		return ResponseEntity.noContent().build();
	}
}
