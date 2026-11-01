package is.hi.store.controller;

import is.hi.store.dto.AdminUpdateUserRequest;
import is.hi.store.dto.CreateAdminRequest;
import is.hi.store.dto.RegisterResponse;
import is.hi.store.dto.TemporaryPasswordResponse;
import is.hi.store.dto.UserSummary;
import is.hi.store.entity.User.Role;
import is.hi.store.entity.User;
import is.hi.store.service.AuthService;
import is.hi.store.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// UC16, UC7. Admin only, enforced in SecurityConfig (/api/admin/** requires ROLE_ADMIN), not here.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AuthService authService;
	private final UserService userService;

	public AdminController(AuthService authService, UserService userService) {
		this.authService = authService;
		this.userService = userService;
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<UserSummary> updateUser(Authentication auth, @PathVariable long id, @RequestBody AdminUpdateUserRequest request) {
		return ResponseEntity.ok(new UserSummary(userService.adminUpdateUser(auth.getName(), id, request)));
	}

	@PostMapping("/users/{id}/reset-password")
	public ResponseEntity<TemporaryPasswordResponse> resetPassword(@PathVariable long id) {
		return ResponseEntity.ok(new TemporaryPasswordResponse(userService.resetPassword(id)));
	}

	@PostMapping("/users")
	public ResponseEntity<RegisterResponse> createAdmin(@RequestBody CreateAdminRequest request) {
		User admin = authService.register(request, Role.ADMIN);
		RegisterResponse response = new RegisterResponse(admin);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
