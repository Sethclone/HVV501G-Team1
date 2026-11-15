package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;

public class ConfirmedState extends OrderState {
	public void transition(Order order, OrderStatus targetStatus) {
		switch(targetStatus) {
			case OrderStatus.SHIPPED:
				order.setState(new ShippedState()); break;
			case OrderStatus.CANCELLED:
				order.setState(new CancelledState()); break;
			default:
				throwInvalidTransition(order.getStatus(), targetStatus);
		}

		order.setStatus(targetStatus);
	}
}
