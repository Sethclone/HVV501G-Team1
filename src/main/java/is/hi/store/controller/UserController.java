package is.hi.store.controller;

import is.hi.store.dto.UpdateAccountRequest;
import is.hi.store.dto.UserSummary;
import is.hi.store.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// UC6. Any authenticated user - covered by anyRequest().authenticated() in SecurityConfig.
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PutMapping("/me")
	public ResponseEntity<UserSummary> updateOwnAccount(Authentication auth, @RequestBody UpdateAccountRequest request) {
		// auth.getName() is the email from the JWT subject (see JwtAuthenticationFilter)
		return ResponseEntity.ok(new UserSummary(userService.updateOwnAccount(auth.getName(), request)));
	}
}
