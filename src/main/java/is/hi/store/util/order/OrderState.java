package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.dto.FieldErrorDetail;

import java.util.*;

public abstract class OrderState {
	protected void throwInvalidTransition(OrderStatus currentStatus, OrderStatus targetStatus) {
		String errorMessage = 
		"Order status transition illegal: " +
		currentStatus + " to " + targetStatus;

		throw new InvalidRequestException(List.of(new FieldErrorDetail("status", errorMessage)));
	}

	public void transition(Order order, OrderStatus targetStatus) {
		System.out.println(order.getStatus() + " -> " + targetStatus);
		if(order.getStatus() == targetStatus) 
			throwInvalidTransition(order.getStatus(), targetStatus);
	}
}
