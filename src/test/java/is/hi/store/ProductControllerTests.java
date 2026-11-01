package is.hi.store;

import com.jayway.jsonpath.JsonPath;
import is.hi.store.dto.RegisterRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

	private Product productWithStock(int stock) {
		Product product = new Product();
		product.setName("Widget");
		product.setStockQuantity(stock);
		return productRepository.save(product);
	}

	private org.springframework.test.web.servlet.ResultActions move(String token, long productId, String json) throws Exception {
		return mvc.perform(post("/api/products/" + productId + "/stock-movements").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private org.springframework.test.web.servlet.ResultActions flag(String token, long productId, String json) throws Exception {
		return mvc.perform(patch("/api/products/" + productId + "/reorder-flag").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
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
}
