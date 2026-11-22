package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;

public class ShippedState extends OrderState{
	public void transition(Order order, OrderStatus targetStatus) {
		switch(targetStatus) {
			case OrderStatus.CANCELLED:
				order.setState(new CancelledState()); break;
			default:
				throwInvalidTransition(order.getStatus(), targetStatus);
		}

		order.setStatus(targetStatus);
	}
}
