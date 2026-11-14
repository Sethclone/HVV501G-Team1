package is.hi.store.util.order;

import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.dto.FieldErrorDetail;

import java.util.*;
public class CancelledState extends OrderState {
	public void transition(Order order, OrderStatus targetStatus) {
		throwInvalidTransition(order.getStatus(), targetStatus);
	}
}
