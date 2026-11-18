package is.hi.store;

import com.jayway.jsonpath.JsonPath;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.dto.OrderItemRequest;
import is.hi.store.entity.Product;
import is.hi.store.entity.User.Role;
import is.hi.store.repository.ProductRepository;
import is.hi.store.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.*;
import java.math.BigDecimal;

// UC1 - POST /api/products/{id}/stock-movements, UC9 - PATCH /api/products/{id}/reorder-flag
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private AuthService authService;
	@Autowired
	private ProductRepository productRepository;

	// Context is shared across test classes, so every test registers its own email.
	private String staffToken(String email) throws Exception {
		authService.register(new RegisterRequest("Staff", email, "password1"), Role.STAFF);
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"password1\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

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

	private Product productWithStock(int stock) {
		Product product = new Product();
		product.setName("Widget");
		product.setStockQuantity(stock);
		return productRepository.save(product);
	}
	private Product fullProduct(String name, String category, int stock, int price) {
		Product product = new Product();
		product.setName(name);
		product.setCategory(category);
		product.setStockQuantity(stock);
		product.setPrice(new BigDecimal(price));
		return productRepository.save(product);
	}
	private Product productWithStockAndPrice(int stock, int price) {
		Product product = new Product();
		product.setName("Widget");
		product.setStockQuantity(stock);
		product.setPrice(new BigDecimal(price));
		return productRepository.save(product);
	}

	private Product productWithPriceAndCategory(int price, String category) {
		Product product = new Product();
		product.setName("Widget");
		product.setStockQuantity(5);
		product.setPrice(new BigDecimal(price));
		product.setCategory(category);
		return productRepository.save(product);
	}

	private org.springframework.test.web.servlet.ResultActions move(String token, long productId, String json) throws Exception {
		return mvc.perform(post("/api/products/" + productId + "/stock-movements").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private org.springframework.test.web.servlet.ResultActions createOrder(String token, String json) throws Exception {
		return mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private org.springframework.test.web.servlet.ResultActions flag(String token, long productId, String json) throws Exception {
		return mvc.perform(patch("/api/products/" + productId + "/reorder-flag").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}
	private org.springframework.test.web.servlet.ResultActions viewProduct(String token, long productId) throws Exception {
		return mvc.perform(get("/api/products/" + productId).header("Authorization", "Bearer " + token));
	}

	private org.springframework.test.web.servlet.ResultActions filterProducts(String token, String category, int minPrice, int maxPrice) throws Exception {
		return mvc.perform(get("/api/products?category="+category+"&minPrice="+maxPrice+"&maxPrice="+maxPrice).header("Authorization", "Bearer " + token));
	}
	
	private org.springframework.test.web.servlet.ResultActions deleteProduct(String token, long productId) throws Exception {
		return mvc.perform(delete("/api/products/" + productId).header("Authorization", "Bearer " + token));
	}

	private int stockOf(Product product) {
		return productRepository.findById(product.getId()).orElseThrow().getStockQuantity();
	}

	// UC1

	@Test
	void addsAndRemovesStock() throws Exception {
		String token = staffToken("sm-ok@example.com");
		Product product = productWithStock(5);
		move(token, product.getId(), "{\"quantity\":3,\"type\":\"ADD\"}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.newStockLevel").value(8));
		move(token, product.getId(), "{\"quantity\":8,\"type\":\"REMOVE\"}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.newStockLevel").value(0));
	}

	@Test
	void rejectsZeroAndMissingQuantity() throws Exception {
		String token = staffToken("sm-zero@example.com");
		Product product = productWithStock(5);
		move(token, product.getId(), "{\"quantity\":0,\"type\":\"ADD\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("quantity"));
		move(token, product.getId(), "{\"type\":\"ADD\"}")
			.andExpect(status().isBadRequest());
		assertEquals(5, stockOf(product));
	}

	@Test
	void rejectsMissingTypeWithoutTouchingStock() throws Exception {
		String token = staffToken("sm-type@example.com");
		Product product = productWithStock(5);
		// Used to fall through to the subtract branch and take stock to -994.
		move(token, product.getId(), "{\"quantity\":999}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("type"));
		assertEquals(5, stockOf(product));
	}

	@Test
	void rejectsOverRemoval() throws Exception {
		String token = staffToken("sm-over@example.com");
		Product product = productWithStock(5);
		move(token, product.getId(), "{\"quantity\":6,\"type\":\"REMOVE\"}")
			.andExpect(status().isBadRequest());
		assertEquals(5, stockOf(product));
	}

	@Test
	void rejectsNonNumericQuantityWith400() throws Exception {
		String token = staffToken("sm-nan@example.com");
		Product product = productWithStock(5);
		// Used to come back as 401 because the error forward to /error hit security.
		move(token, product.getId(), "{\"quantity\":\"abc\",\"type\":\"ADD\"}")
			.andExpect(status().isBadRequest());
	}

	@Test
	void missingProductIs404() throws Exception {
		String token = staffToken("sm-404@example.com");
		move(token, 999999, "{\"quantity\":1,\"type\":\"ADD\"}")
			.andExpect(status().isNotFound());
	}

	@Test
	void productCreatedWithoutStockStartsAtZero() throws Exception {
		String token = staffToken("sm-nostock@example.com");
		// Used to save a null stock, then any stock movement on it was a 500.
		String body = mvc.perform(multipart("/api/products").param("name", "No stock").param("category", "Tools").param("price", "9.99")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.stockQuantity").value(0))
			.andReturn().getResponse().getContentAsString();
		long id = ((Number) JsonPath.read(body, "$.id")).longValue();
		move(token, id, "{\"quantity\":2,\"type\":\"ADD\"}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.newStockLevel").value(2));
	}

	@Test
	void createRejectsMissingFields() throws Exception {
		String token = staffToken("p-missing@example.com");
		// A product without a price used to save, then any order containing it was a 500.
		mvc.perform(multipart("/api/products").param("stockQuantity", "-1").header("Authorization", "Bearer " + token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.length()").value(4));
	}

	// UC9

	@Test
	void flagsAndUnflagsProduct() throws Exception {
		String token = staffToken("rf-ok@example.com");
		Product product = productWithStock(5);
		flag(token, product.getId(), "{\"flagged\":true}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(product.getId()))
			.andExpect(jsonPath("$.reorderFlagged").value(true));
		flag(token, product.getId(), "{\"flagged\":false}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.reorderFlagged").value(false));
	}

	@Test
	void emptyBodyDoesNotUnflag() throws Exception {
		String token = staffToken("rf-empty@example.com");
		Product product = productWithStock(5);
		flag(token, product.getId(), "{\"flagged\":true}").andExpect(status().isOk());
		flag(token, product.getId(), "{}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("flagged"));
		flag(token, product.getId(), "{\"flagged\":\"cat\"}")
			.andExpect(status().isBadRequest());
		assertEquals(true, productRepository.findById(product.getId()).orElseThrow().isReorderFlagged());
	}

	@Test
	void flagMissingProductIs404() throws Exception {
		String token = staffToken("rf-404@example.com");
		flag(token, 999999, "{\"flagged\":true}")
			.andExpect(status().isNotFound());
	}

	//UC10
	@Test
	void deletingProductWithMovementRecordIs204() throws Exception {	
		String token = rootToken();
		Product product = productWithStock(5);
		move(token, product.getId(), "{\"quantity\":2,\"type\":\"REMOVE\"}").andExpect(status().isOk());

		deleteProduct(token, product.getId())
			.andExpect(status().isNoContent());
	}

	@Test
	void deletingProductWithOrderItemIs409() throws Exception {
		String token = rootToken();
		Product product = productWithStockAndPrice(20,100);

		List<OrderItemRequest> items = List.of(
			new OrderItemRequest(product.getId(), 6)
		);

		createOrder(token, "{\"items\":"+items+"}");
		deleteProduct(token,product.getId())
			.andExpect(status().isConflict());
	}

	//UC3
	@Test
	void filterProductsSuccess() throws Exception {
		String token = staffToken("filter-success@example.com");

		Product product1 = productWithPriceAndCategory(100, "Tool");
		Product product2 = productWithPriceAndCategory(20, "Tool");
		Product product3 = productWithPriceAndCategory(50, "Food");
		Product product4 = productWithPriceAndCategory(200, "Tool");
		Product product5 = productWithPriceAndCategory(300, "Shoe");

		String bodyPending = filterProducts(token, "Tool", 100, 200)
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();

		List<String> category = JsonPath.read(bodyPending, "$.content[*].category");
		List<Double> price = JsonPath.read(bodyPending, "$.content[*].price");
		
		assertTrue(category.stream().allMatch(i -> i.equals("Tool")));
		assertTrue(price.stream().allMatch(i -> i >= 100));
		assertTrue(price.stream().allMatch(i -> i <= 200));
	}

	//UC8
	@Test
	void viewProductSuccess() throws Exception {
		String token = staffToken("view-product-success@example.com");

		Product product = fullProduct("Shoe", "Clothing", 100, 200);

		viewProduct(token, product.getId())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Shoe"))
			.andExpect(jsonPath("$.category").value("Clothing"))
			.andExpect(jsonPath("$.stockQuantity").value(100))
			.andExpect(jsonPath("$.price").value(200));
	}
}
