package is.hi.store.dto;

import java.util.List;

public class UpdateOrderRequest {

    private List<OrderItemRequest> items;

    public UpdateOrderRequest() {}

    public UpdateOrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
