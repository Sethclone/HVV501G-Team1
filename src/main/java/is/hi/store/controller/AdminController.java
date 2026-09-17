package is.hi.store.controller;

import is.hi.store.dto.CreateAdminRequest;
import is.hi.store.dto.RegisterResponse;
import is.hi.store.entity.User.Role;
import is.hi.store.service.RegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// UC16. Admin-only - enforced in SecurityConfig (/api/admin/** requires ROLE_ADMIN), not here.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final RegisterService registerService;

	public AdminController(RegisterService registerService) {
		this.registerService = registerService;
	}

	@PostMapping("/users")
	public ResponseEntity<RegisterResponse> createAdmin(@RequestBody CreateAdminRequest request) {
		RegisterResponse response = registerService.register(request.getName(), request.getEmail(), request.getPassword(), Role.ADMIN);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
