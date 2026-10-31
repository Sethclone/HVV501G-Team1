package is.hi.store;

import com.jayway.jsonpath.JsonPath;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.entity.User.Role;
import is.hi.store.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// UC7: PUT /api/admin/users/{id}, POST /api/admin/users/{id}/reset-password
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private AuthService authService;

	// Seeded on startup by AuthController's CommandLineRunner.
	private String rootToken() throws Exception {
		return login("root@example.com", "password");
	}

	private long staff(String email) {
		return authService.register(new RegisterRequest("Staff", email, "oldpassword"), Role.STAFF).getId();
	}

	private String login(String email, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	private ResultActions update(String token, long id, String json) throws Exception {
		return mvc.perform(put("/api/admin/users/" + id).header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private ResultActions resetPassword(String token, long id) throws Exception {
		return mvc.perform(post("/api/admin/users/" + id + "/reset-password").header("Authorization", "Bearer " + token));
	}

	private ResultActions listOrders(String token) throws Exception {
		return mvc.perform(get("/api/orders").header("Authorization", "Bearer " + token));
	}

	@Test
	void updatesNameEmailAndRole() throws Exception {
		long id = staff("a-update@example.com");
		update(rootToken(), id, "{\"name\":\"Renamed\",\"email\":\"a-update2@example.com\",\"role\":\"ADMIN\"}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Renamed"))
			.andExpect(jsonPath("$.email").value("a-update2@example.com"))
			.andExpect(jsonPath("$.role").value("ADMIN"));
	}

	@Test
	void staffIsForbidden() throws Exception {
		long id = staff("a-forbidden@example.com");
		update(login("a-forbidden@example.com", "oldpassword"), id, "{\"role\":\"ADMIN\"}")
			.andExpect(status().isForbidden());
	}

	@Test
	void unknownUserIs404() throws Exception {
		update(rootToken(), 999999, "{\"name\":\"x\"}").andExpect(status().isNotFound());
		resetPassword(rootToken(), 999999).andExpect(status().isNotFound());
	}

	@Test
	void rejectsBadAndTakenEmail() throws Exception {
		long id = staff("a-email@example.com");
		staff("a-taken@example.com");
		update(rootToken(), id, "{\"email\":\"not-an-email\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
		update(rootToken(), id, "{\"email\":\"a-taken@example.com\"}")
			.andExpect(status().isConflict());
	}

	@Test
	void adminCannotDemoteSelf() throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"root@example.com\",\"password\":\"password\"}"))
			.andReturn().getResponse().getContentAsString();
		long rootId = ((Number) JsonPath.read(body, "$.user.id")).longValue();
		update(JsonPath.read(body, "$.token"), rootId, "{\"role\":\"STAFF\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("role"));
	}

	@Test
	void demotionAppliesToExistingToken() throws Exception {
		long id = staff("a-demote@example.com");
		update(rootToken(), id, "{\"role\":\"ADMIN\"}").andExpect(status().isOk());
		String adminToken = login("a-demote@example.com", "oldpassword");
		update(adminToken, id, "{\"name\":\"Still admin\"}").andExpect(status().isOk());

		update(rootToken(), id, "{\"role\":\"STAFF\"}").andExpect(status().isOk());
		// Same token, but the role is read from the DB now, not the token's claim.
		update(adminToken, id, "{\"role\":\"ADMIN\"}").andExpect(status().isForbidden());
	}

	@Test
	void emailChangeKeepsExistingToken() throws Exception {
		long id = staff("a-move@example.com");
		String token = login("a-move@example.com", "oldpassword");
		update(rootToken(), id, "{\"email\":\"a-moved@example.com\"}").andExpect(status().isOk());
		mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("a-moved@example.com"));
	}

	@Test
	void resetForcesPasswordChange() throws Exception {
		long id = staff("a-reset@example.com");
		String body = resetPassword(rootToken(), id)
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		String temp = JsonPath.read(body, "$.temporaryPassword");

		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"a-reset@example.com\",\"password\":\"oldpassword\"}"))
			.andExpect(status().isUnauthorized());
		String loginBody = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"a-reset@example.com\",\"password\":\"" + temp + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.user.mustChangePassword").value(true))
			.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(loginBody, "$.token");

		listOrders(token).andExpect(status().isForbidden());
		mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
				.content("{\"password\":\"newpassword\",\"currentPassword\":\"" + temp + "\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.mustChangePassword").value(false));
		listOrders(token).andExpect(status().isOk());
	}
}
