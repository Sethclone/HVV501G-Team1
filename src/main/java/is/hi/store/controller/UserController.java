package is.hi.store.controller;

import is.hi.store.dto.UpdateAccountRequest;
import is.hi.store.dto.UserSummary;
import is.hi.store.service.UserService;
import is.hi.store.dto.UploadProfilePicRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

// UC6. Any STAFF or ADMIN user (anyRequest() in SecurityConfig). PUT /me has its own rule so a user with a
// pending password change (UC7 reset) can still reach it.
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PutMapping("/me")
	public ResponseEntity<UserSummary> updateOwnAccount(Authentication auth, @RequestBody UpdateAccountRequest request) {
		// auth.getName() is the user's current email, loaded from the DB by JwtAuthenticationFilter
		return ResponseEntity.ok(new UserSummary(userService.updateOwnAccount(auth.getName(), request)));
	}

	@PostMapping("/me/profile-picture")
	public ResponseEntity<UserSummary> uploadProfilePicture(Authentication auth, @ModelAttribute UploadProfilePicRequest request) {
		return ResponseEntity.ok(new UserSummary(userService.uploadProfilePicture(auth.getName(), request)));
	}
}
