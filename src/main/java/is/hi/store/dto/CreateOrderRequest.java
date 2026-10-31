package is.hi.store.dto;

import java.util.List;

public class CreateOrderRequest {

    private List<OrderItemRequest> items;

    public CreateOrderRequest() {}

    public CreateOrderRequest(List<OrderItemRequest> items) {
        this.items = items;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
