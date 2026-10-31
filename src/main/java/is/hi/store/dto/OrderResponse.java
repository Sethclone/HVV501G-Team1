package is.hi.store.dto;

import is.hi.store.entity.Order;
import is.hi.store.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private Instant createdAt;
    private List<OrderItemResponse> items;

    public OrderResponse(Order order){
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.status = order.getStatus();
        this.totalPrice = order.getTotalPrice();
        this.createdAt = order.getCreatedAt();
        this.items = order.getItems().stream().map(OrderItemResponse::new).toList();
        
    }
}
