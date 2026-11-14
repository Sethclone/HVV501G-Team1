package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.dto.FieldErrorDetail;

import java.util.*;
public class PendingState extends OrderState {
	public void transition(Order order, OrderStatus targetStatus) {
		super.transition(order,targetStatus);

		switch(targetStatus) {
			case OrderStatus.CONFIRMED:
				order.setState(new ConfirmedState());
				break;
			case OrderStatus.CANCELLED:
				order.setState(new CancelledState());
				break;
			default:
				throwInvalidTransition(order.getStatus(), targetStatus);
		}

		order.setStatus(targetStatus);
	}
}
