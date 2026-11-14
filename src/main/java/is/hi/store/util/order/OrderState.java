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
		if(order.getStatus() == targetStatus) 
			throwInvalidTransition(order.getStatus(), targetStatus);
	}

	public static OrderState fromStatus(OrderStatus status) {	
		switch(status) {
			case OrderStatus.PENDING:
				return new PendingState();
			case OrderStatus.CONFIRMED:
				return new ConfirmedState();
			case OrderStatus.SHIPPED:
				return new ShippedState();
			case OrderStatus.CANCELLED:
				return new CancelledState();
			default:
				return null;
		}
	}
}
