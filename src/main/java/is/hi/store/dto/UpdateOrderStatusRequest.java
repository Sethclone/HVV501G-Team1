package is.hi.store.dto;

import java.util.List;
import is.hi.store.entity.Order.OrderStatus;

public class UpdateOrderStatusRequest {

    private OrderStatus status;

    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
