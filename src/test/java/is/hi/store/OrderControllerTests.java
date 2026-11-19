package is.hi.store;

import com.jayway.jsonpath.JsonPath;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.dto.OrderItemRequest;
import is.hi.store.dto.OrderResponse;
import is.hi.store.entity.Product;
import is.hi.store.entity.Order;
import is.hi.store.entity.User.Role;
import is.hi.store.entity.Order.OrderStatus;
import is.hi.store.repository.OrderRepository;
import is.hi.store.repository.ProductRepository;
import is.hi.store.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import java.math.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private AuthService authService;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ProductRepository productRepository;

	private String rootToken() throws Exception {
		return login("root@example.com", "password");
	}

	private String login(String email, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	private String staffToken(String email) throws Exception {
		authService.register(new RegisterRequest("Staff", email, "password1"), Role.STAFF);
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"password1\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	private Product productWithStock(int stock, int price) {
		Product product = new Product();
		product.setName("Widget");
		product.setStockQuantity(stock);
		product.setPrice(new BigDecimal(price));
		return productRepository.save(product);
	}

	private org.springframework.test.web.servlet.ResultActions createOrder(String token, String json) throws Exception {
		return mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private org.springframework.test.web.servlet.ResultActions updateStatus(String token, long orderId,String status) throws Exception {
		return mvc.perform(patch("/api/orders/"+orderId+"/status").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content("{\"status\":\""+status+"\"}"));
	}

	private Long createOrderAndReturnId(String token, String json) throws Exception {	
		return Long.valueOf(
			JsonPath.read(createOrder(token, json)
			.andReturn().getResponse().getContentAsString(), "$.id")
			.toString()
		);
	}

	private Long getOrderIdFromResponse(String json) {
		return Long.valueOf(
			JsonPath.read(json,"$.id").toString()
		);
	}

	private Long getOrderItemIdFromResponse(String json, int index) {
		return Long.valueOf(
			JsonPath.read(json,"$.items["+index+"].id").toString()
		);
	}

	private org.springframework.test.web.servlet.ResultActions getAllOrders(String token, String status, int page, int size ) throws Exception {
		return mvc.perform(get("/api/orders?status="+status+"&page="+page+"&size="+size).header("Authorization", "Bearer " + token));
	}

	private org.springframework.test.web.servlet.ResultActions getOrder(String token, long id) throws Exception {
		return mvc.perform(get("/api/orders/"+id).header("Authorization", "Bearer " + token));
	}

	private org.springframework.test.web.servlet.ResultActions removeItem(String token, long orderId, long itemId) throws Exception {
		return mvc.perform(delete("/api/orders/"+orderId+"/items/"+itemId).header("Authorization", "Bearer " + token));
	}

	private org.springframework.test.web.servlet.ResultActions removeOrder(String token, long orderId) throws Exception {
		return mvc.perform(delete("/api/orders/"+orderId).header("Authorization", "Bearer " + token));
	}

	private org.springframework.test.web.servlet.ResultActions updateOrder(String token, long orderId, String json) throws Exception {
		return mvc.perform(put("/api/orders/" + orderId).header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	//Tests for UC11, UC12, UC17, UC18, UC13
	// UC11
	@Test
	void createOrderSuccess() throws Exception {
		String token = staffToken("order-success@example.com");
		Product product1 = productWithStock(5,100);
		Product product2 = productWithStock(8,200);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product1.getId(), 3),
			new OrderItemRequest(product2.getId(), 5)
		);

		createOrder(token, "{\"items\":"+items+"}")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.totalPrice").value(100*3 + 200*5))
			.andExpect(jsonPath("$.status").value("PENDING"));

		product1 = productRepository.findById(product1.getId()).orElse(null);
		product2 = productRepository.findById(product2.getId()).orElse(null);

		assertNotNull(product1);
		assertNotNull(product2);
		assertEquals(2, product1.getStockQuantity());
		assertEquals(3, product2.getStockQuantity());
	}

	@Test
	void createOrderExceedsProductQuantity() throws Exception {
		String token = staffToken("order-exceeds@example.com");
		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(), 6)
		);

		createOrder(token, "{\"items\":"+items+"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"));

		product = productRepository.findById(product.getId()).orElse(null);

		assertNotNull(product);
		assertEquals(5, product.getStockQuantity());
	}

	@Test
	void rejectIfQuantityIsZero() throws Exception {
		String token = staffToken("order-zero@example.com");
		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(), 0)
		);

		createOrder(token, "{\"items\":"+items+"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"));

		product = productRepository.findById(product.getId()).orElse(null);

		assertNotNull(product);
		assertEquals(5, product.getStockQuantity());
	}

	@Test
	void rejectIfItemListIsEmpty() throws Exception {
		String token = staffToken("order-empty@example.com");

		List<OrderItemRequest> items = new ArrayList<>();

		createOrder(token, "{\"items\":"+items+"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("items"));
	}

	@Test
	void NonExistentProductIs404() throws Exception {
		String token = staffToken("order-ne@example.com");

		Product product = productWithStock(10, 200);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId()+2000,2) 
		);

		createOrder(token, "{\"items\":"+items+"}")
			.andExpect(status().isNotFound());
	}

	// UC12 and UC17

	@Test
	void viewAllOrdersSuccess() throws Exception {
		String token = staffToken("view-all-success@example.com");

		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		long orderId1 = createOrderAndReturnId(token, "{\"items\":"+items+"}");
		long orderId2 = createOrderAndReturnId(token, "{\"items\":"+items+"}");

		Order order2 = orderRepository.findById(orderId2).orElse(null);
		order2.setStatus(OrderStatus.CONFIRMED);
		orderRepository.save(order2);

		String bodyPending = getAllOrders(token, "PENDING", 0, 10)
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		String bodyConfirmed = getAllOrders(token, "CONFIRMED", 0, 10)
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		List<String> contentPending = JsonPath.read(bodyPending, "$.content[*].status");
		List<String> contentConfirmed = JsonPath.read(bodyConfirmed, "$.content[*].status");
		
		assertTrue(contentPending.stream().allMatch(i -> i.equals("PENDING")));
		assertTrue(contentConfirmed.stream().allMatch(i -> i.equals("CONFIRMED")));
	}

	@Test
	void viewAllOrdersInvalidParameters() throws Exception {
		String token = staffToken("view-all-invalid@example.com");

		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		createOrder(token, "{\"items\":"+items+"}");
		createOrder(token, "{\"items\":"+items+"}");

		getAllOrders(token, "PENDING", -1, 10)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("page"));
		getAllOrders(token, "PENDING", 0, 0)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("size"));
	}

	@Test
	void viewOrderSuccess() throws Exception {
		String token = staffToken("view-success@example.com");

		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		long id = createOrderAndReturnId(token, "{\"items\":"+items+"}");

		getOrder(token, id)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].productId").value(items.get(0).getProductId()))
			.andExpect(jsonPath("$.items[0].quantity").value(items.get(0).getQuantity()));
	}

	@Test
	void viewOrderNotFound() throws Exception {
		String token = staffToken("view-not-found@example.com");

		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		long id = createOrderAndReturnId(token, "{\"items\":"+items+"}");

		getOrder(token, id+200)
			.andExpect(status().isNotFound());
	}

	//UC18
	@Test
	void removeItemSuccess() throws Exception {
		String token = staffToken("remove-item-success@example.com");
		Product product = productWithStock(5,100);
		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		String response = createOrder(token, "{\"items\":"+items+"}")
			.andReturn().getResponse().getContentAsString();

		long orderId = getOrderIdFromResponse(response);
		long orderItemId = getOrderItemIdFromResponse(response, 0);

		removeItem(token, orderId, orderItemId)
			.andExpect(status().isOk());

		product = productRepository.findById(product.getId()).orElse(null);
		assertEquals(5, product.getStockQuantity());
	}

	@Test
	void removeItemNotPending() throws Exception {
		String token = staffToken("remove-item-np@example.com");
		Product product = productWithStock(5,100);
		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		String response = createOrder(token, "{\"items\":"+items+"}")
			.andReturn().getResponse().getContentAsString();

		long orderId = getOrderIdFromResponse(response);
		long orderItemId = getOrderItemIdFromResponse(response,0);

		updateStatus(token, orderId, "CONFIRMED")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CONFIRMED"));
		removeItem(token, orderId, 0)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
	}
	
	//UC13
	@Test
	void removeOrderSuccess() throws Exception {
		String token = rootToken();
		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		long id = createOrderAndReturnId(token, "{\"items\":"+items+"}");

		updateStatus(token, id, "CANCELLED")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"));

		removeOrder(token, id)
			.andExpect(status().isNoContent());

		product = productRepository.findById(product.getId()).orElse(null);
		assertEquals(5, product.getStockQuantity());
	}

	@Test
	void removeOrderNotCancelled() throws Exception {
		String token = rootToken();
		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		long id = createOrderAndReturnId(token, "{\"items\":"+items+"}");

		removeOrder(token, id)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
	}
	
	@Test
	void invalidStatusTransitions() throws Exception {
		String token = staffToken("status-invalid@example.com");
		Product product = productWithStock(5,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(),2) 
		);

		long id = createOrderAndReturnId(token, "{\"items\":"+items+"}");

		updateStatus(token, id, "SHIPPED")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
	}

	//UC14
	@Test
	void updateOrderSuccess() throws Exception {
		String token = staffToken("update-order-ok@example.com");
		Product p1 = productWithStock(5, 100);
		Product p2 = productWithStock(10, 50);

		long orderId = createOrderAndReturnId(token, "{\"items\":" + List.of(new OrderItemRequest(p1.getId(), 3)) + "}");

		p1 = productRepository.findById(p1.getId()).orElseThrow();
		p1.setPrice(new BigDecimal(150));
		productRepository.save(p1);

		List<OrderItemRequest> updatedItems = List.of(
			new OrderItemRequest(p1.getId(), 4),
			new OrderItemRequest(p2.getId(), 2)
		);

		updateOrder(token, orderId, "{\"items\":" + updatedItems + "}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(2))
			.andExpect(jsonPath("$.totalPrice").value(700));

		assertEquals(1, productRepository.findById(p1.getId()).orElseThrow().getStockQuantity());
		assertEquals(8, productRepository.findById(p2.getId()).orElseThrow().getStockQuantity());
	}

	@Test
	void updateOrderNotPending() throws Exception {
		String token = staffToken("update-order-np@example.com");
		Product p = productWithStock(5, 100);
		long orderId = createOrderAndReturnId(token, "{\"items\":" + List.of(new OrderItemRequest(p.getId(), 2)) + "}");

		updateStatus(token, orderId, "CONFIRMED").andExpect(status().isOk());

		updateOrder(token, orderId, "{\"items\":" + List.of(new OrderItemRequest(p.getId(), 1)) + "}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("status"));
	}

	@Test
	void updateOrderInsufficientStock() throws Exception {
		String token = staffToken("update-order-stock@example.com");
		Product p = productWithStock(5, 100);
		long orderId = createOrderAndReturnId(token, "{\"items\":" + List.of(new OrderItemRequest(p.getId(), 2)) + "}");

		updateOrder(token, orderId, "{\"items\":" + List.of(new OrderItemRequest(p.getId(), 6)) + "}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"));

		assertEquals(3, productRepository.findById(p.getId()).orElseThrow().getStockQuantity());
	}

	@Test
	void updateOrderInvalidItemsAndNotFound() throws Exception {
		String token = staffToken("update-order-bad@example.com");
		Product p = productWithStock(5, 100);
		long orderId = createOrderAndReturnId(token, "{\"items\":" + List.of(new OrderItemRequest(p.getId(), 1)) + "}");

		updateOrder(token, orderId, "{\"items\":[]}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("items"));

		updateOrder(token, 999999L, "{\"items\":" + List.of(new OrderItemRequest(p.getId(), 1)) + "}")
			.andExpect(status().isNotFound());
	}

}
