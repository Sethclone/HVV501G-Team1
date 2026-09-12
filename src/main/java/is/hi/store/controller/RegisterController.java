package is.hi.store.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import is.hi.store.entity.User;
import is.hi.store.service.UserService;

@RestController
public class RegisterController {
	
	@Autowired
	UserService userService;

	@PostMapping("/api/auth/register")
	public String hello(
		@RequestParam(value="username") String username,
		@RequestParam(value="password") String password
	) {
		return String.format("Hello", username);
	}
}
