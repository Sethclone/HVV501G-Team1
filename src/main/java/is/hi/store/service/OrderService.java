package is.hi.store.service;

import is.hi.store.dto.*;
import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.entity.OrderItem;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.exception.*;
import is.hi.store.repository.OrderRepository;
import is.hi.store.repository.ProductRepository;
import is.hi.store.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.*;

public interface OrderService {
    OrderResponse createOrder(String email, CreateOrderRequest request);
	OrderResponse removeItem(Long orderId, Long itemId);
    OrderResponse getOrderById(Long id);
    PagedResponse<OrderResponse> getOrders(String status, int page, int size);
	OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);
    OrderResponse updateOrder(Long id, UpdateOrderRequest request);
	void deleteOrder(Long id);
    OrderResponse reassignOrder(Long orderId, ReassignOrderRequest request);
}

@Service
class OrderServiceImplementation implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImplementation(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Account no longer exists"));

        validateItems(request == null ? null : request.getItems());

        Map<Long, Product> productsById = new HashMap<>();
        loadAndVerifyStock(request.getItems(), productsById);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productsById.get(itemRequest.getProductId());
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItems.add(orderItem);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(subtotal);
        }

        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    } 

	@Transactional
	public OrderResponse removeItem(Long orderId, Long itemId) {
		List<FieldErrorDetail> errors = new ArrayList<FieldErrorDetail>();
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));

		if(order.getStatus() != OrderStatus.PENDING) {
			errors.add(new FieldErrorDetail("status", "Must be PENDING"));
			throw new InvalidRequestException(errors);
		}

		OrderItem item = 
		order
		.getItems()
		.stream()
		.filter(i -> i.getId().equals(itemId))
		.findFirst()
		.orElseThrow(() -> new ResourceNotFoundException("Order Item", itemId));

		Product product = item.getProduct();

		product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
		order.getItems().remove(item);
		order.setTotalPrice(
			order.getItems()
			.stream()
			.map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
			.reduce(BigDecimal.ZERO, (a,b) -> a.add(b))
			);

		productRepository.save(product);
		orderRepository.save(order);

		return new OrderResponse(order);
	}

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setStatus(order.getStatus());
        response.setTotalPrice(order.getTotalPrice());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemResponse itemResponse = new OrderItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setQuantity(item.getQuantity());
            itemResponse.setUnitPrice(item.getUnitPrice());
            itemResponse.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemResponses.add(itemResponse);
        }
        response.setItems(itemResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrders(String status, int page, int size) {
        List<FieldErrorDetail> errors = new ArrayList<>();

        if (page < 0) {
            errors.add(new FieldErrorDetail("page", "must be 0 or greater"));
        }
        if (size < 1) {
            errors.add(new FieldErrorDetail("size", "must be at least 1"));
        }

        OrderStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = OrderStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add(new FieldErrorDetail("status",
                        "must be one of PENDING, CONFIRMED, SHIPPED, CANCELLED"));
            }
        }
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> result = (statusFilter == null)
                ? orderRepository.findAll(pageable)
                : orderRepository.findByStatus(statusFilter, pageable);

        List<OrderResponse> content = result.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PagedResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

	@Transactional
	public OrderResponse updateOrderStatus(
		Long id,
		UpdateOrderStatusRequest request
	) {
		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

		if(request == null || request.getStatus() == null) 
			throw new InvalidRequestException(List.of(new FieldErrorDetail("status", "Request must contain status")));

		OrderStatus currentStatus = order.getStatus();
		OrderStatus targetStatus = request.getStatus();

		order.transition(targetStatus);

		if(targetStatus != OrderStatus.CANCELLED) {
			orderRepository.save(order);
			return new OrderResponse(order);
		}

		List<OrderItem> items = order.getItems();
		List<Product> products = new ArrayList<>();
		for(OrderItem item : items) {
			Product product = item.getProduct();
			product.setStockQuantity(product.getStockQuantity()+item.getQuantity());
			products.add(product);
		}

		productRepository.saveAll(products);
		orderRepository.save(order);
		return new OrderResponse(order);
	}

	@Transactional
	public void deleteOrder(Long id) {	
		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
		if(order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CONFIRMED)
			throw new InvalidRequestException(List.of(new FieldErrorDetail("status", "Orders can only be deleted if they are CANCELLED or SHIPPED")));
		orderRepository.delete(order);
	}

    @Transactional
    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(List.of(new FieldErrorDetail("status", "Only pending orders can be updated")));
        }

        validateItems(request == null ? null : request.getItems());

        // Put old items' stock back first so editing an item already on the order checks against true available stock.
        Map<Long, Product> productsById = new HashMap<>();
        for (OrderItem oldItem : order.getItems()) {
            Product product = oldItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() + oldItem.getQuantity());
            productsById.put(product.getId(), product);
        }

        loadAndVerifyStock(request.getItems(), productsById);

        order.getItems().clear();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productsById.get(itemRequest.getProductId());
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            order.getItems().add(orderItem);

            totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        productRepository.saveAll(productsById.values());
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    private void validateItems(List<OrderItemRequest> items) {
        List<FieldErrorDetail> errors = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            errors.add(new FieldErrorDetail("items", "Order must contain at least one item"));
            throw new InvalidRequestException(errors);
        }

        for (OrderItemRequest itemRequest : items) {
            if (itemRequest == null || itemRequest.getProductId() == null) {
                errors.add(new FieldErrorDetail("productId", "Product id must not be null"));
            }
            if (itemRequest == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                errors.add(new FieldErrorDetail("quantity", "Quantity must be greater than zero"));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }
    }

    private void loadAndVerifyStock(List<OrderItemRequest> items, Map<Long, Product> productsById) {
        Map<Long, Integer> requestedQuantities = new HashMap<>();
        for (OrderItemRequest itemRequest : items) {
            Long productId = itemRequest.getProductId();
            if (!productsById.containsKey(productId)) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
                productsById.put(productId, product);
            }
            requestedQuantities.merge(productId, itemRequest.getQuantity(), Integer::sum);
        }

        List<FieldErrorDetail> errors = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productsById.get(entry.getKey());
            if (product.getStockQuantity() < entry.getValue()) {
                errors.add(new FieldErrorDetail("quantity", "Insufficient stock for product: " + product.getName()));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }
    }
  
    public OrderResponse reassignOrder(Long orderId, ReassignOrderRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new InvalidRequestException(
                    List.of(new FieldErrorDetail("userId", "userId is required")));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        User target = userRepository.findById(request.getUserId());
        if (target == null) {
            throw new UserNotFoundException(request.getUserId());
        }

        if (!Objects.equals(order.getUser().getId(), target.getId())) {
            order.setUser(target);
            orderRepository.save(order);
        }
        return mapToResponse(order);
    }
}
