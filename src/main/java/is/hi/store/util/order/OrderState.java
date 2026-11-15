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

class CancelledState extends OrderState {
}

class ConfirmedState extends OrderState {
	public void transition(Order order, OrderStatus targetStatus) {
		switch(targetStatus) {
			case OrderStatus.SHIPPED:
				order.setState(new ShippedState());
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

class PendingState extends OrderState {
	public void transition(Order order, OrderStatus targetStatus) {
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

class ShippedState extends OrderState{
	public void transition(Order order, OrderStatus targetStatus) {
		switch(targetStatus) {
			case OrderStatus.CANCELLED:
				order.setState(new CancelledState());
				break;
			default:
				throwInvalidTransition(order.getStatus(), targetStatus);
		}

		order.setStatus(targetStatus);
	}
}
