package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.dto.FieldErrorDetail;

import java.util.*;
public class ShippedState extends OrderState{
	public void transition(Order order, OrderStatus targetStatus) {
		super.transition(order,targetStatus);

		switch(targetStatus) {
			case OrderStatus.PENDING:
			case OrderStatus.CONFIRMED:
				throwInvalidTransition(order.getStatus(), targetStatus);
			default:
				break;
		}

		order.setState(new CancelledState());
		order.setStatus(targetStatus);
	}
}
