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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// UC6 - PUT /api/users/me
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private AuthService authService;

	// Closed system: users only exist because an admin made them, so seed via the service.
	private String staffToken(String email) throws Exception {
		authService.register(new RegisterRequest("Staff", email, "oldpassword"), Role.STAFF);
		return login(email, "oldpassword");
	}

	private String login(String email, String password) throws Exception {
		String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
			.andExpect(status().isOk())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	private org.springframework.test.web.servlet.ResultActions updateMe(String token, String json) throws Exception {
		return mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	@Test
	void changesName() throws Exception {
		String token = staffToken("name@example.com");
		updateMe(token, "{\"name\":\"New Name\"}")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("New Name"));
	}

	@Test
	void rejectsBlankNameAndShortPassword() throws Exception {
		String token = staffToken("blank@example.com");
		updateMe(token, "{\"name\":\" \",\"password\":\"short\",\"currentPassword\":\"oldpassword\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.length()").value(2));
	}

	@Test
	void rejectsTooLongNameAndPassword() throws Exception {
		String token = staffToken("long@example.com");
		// Without these checks the DB column (255) and BCrypt (72 bytes) would blow up with a 500.
		updateMe(token, "{\"name\":\"" + "n".repeat(256) + "\",\"password\":\"" + "p".repeat(73) + "\",\"currentPassword\":\"oldpassword\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors.length()").value(2));
	}

	@Test
	void passwordChangeRequiresCurrentPassword() throws Exception {
		String token = staffToken("reauth@example.com");
		updateMe(token, "{\"password\":\"newpassword\"}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.fieldErrors[0].field").value("currentPassword"));
		updateMe(token, "{\"password\":\"newpassword\",\"currentPassword\":\"wrongpassword\"}")
			.andExpect(status().isBadRequest());
	}

	@Test
	void changesPassword() throws Exception {
		String token = staffToken("pw@example.com");
		updateMe(token, "{\"password\":\"newpassword\",\"currentPassword\":\"oldpassword\"}")
			.andExpect(status().isOk());
		login("pw@example.com", "newpassword");
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"pw@example.com\",\"password\":\"oldpassword\"}"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void cannotChangeRoleOrEmail() throws Exception {
		String token = staffToken("locked@example.com");
		// Jackson may ignore or reject the unknown fields - either way they must never be applied.
		updateMe(token, "{\"role\":\"ADMIN\",\"email\":\"hacked@example.com\"}");
		mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + login("locked@example.com", "oldpassword"))
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.role").value("STAFF"))
			.andExpect(jsonPath("$.email").value("locked@example.com"));
	}

	@Test
	void requiresToken() throws Exception {
		mvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"x\"}"))
			.andExpect(status().isUnauthorized());
	}
}
