package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.dto.FieldErrorDetail;

import java.util.*;

public abstract class OrderState {

	public void transition(Order order, OrderStatus targetStatus) {
		throwInvalidTransition(order.getStatus(), targetStatus);
	}

	protected void throwInvalidTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
		String errorMessage = 
		"Order status transition illegal: " +
		currentStatus + " to " + targetStatus;

		throw new InvalidRequestException(List.of(new FieldErrorDetail("status", errorMessage)));
	}
}
