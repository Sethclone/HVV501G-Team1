package is.hi.store.service;

import is.hi.store.dto.CreateOrderRequest;
import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.dto.OrderItemRequest;
import is.hi.store.dto.OrderItemResponse;
import is.hi.store.dto.OrderResponse;
import is.hi.store.entity.Order;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.entity.OrderItem;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.exception.ProductNotFoundException;
import is.hi.store.repository.OrderRepository;
import is.hi.store.repository.ProductRepository;
import is.hi.store.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface OrderService {
    OrderResponse createOrder(String email, CreateOrderRequest request);
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

        List<FieldErrorDetail> errors = new ArrayList<>();
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            errors.add(new FieldErrorDetail("items", "Order must contain at least one item"));
            throw new InvalidRequestException(errors);
        }

        for (OrderItemRequest itemRequest : request.getItems()) {
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

        Map<Long, Product> productsById = new HashMap<>();
        Map<Long, Integer> requestedQuantities = new HashMap<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Long productId = itemRequest.getProductId();
            Product product = productsById.get(productId);
            if (product == null) {
                product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
                productsById.put(productId, product);
            }
            int currentRequested = requestedQuantities.getOrDefault(productId, 0);
            requestedQuantities.put(productId, currentRequested + itemRequest.getQuantity());
        }

        for (Map.Entry<Long, Integer> entry : requestedQuantities.entrySet()) {
            Product product = productsById.get(entry.getKey());
            if (product.getStockQuantity() < entry.getValue()) {
                errors.add(new FieldErrorDetail("quantity", "Insufficient stock for product: " + product.getName()));
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }

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
}
