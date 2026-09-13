package is.hi.store.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.service.RegisterService;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.dto.RegisterResponse;


@RestController
@RequestMapping("/api/auth")
public class RegisterController {
		
	RegisterService registerService;
	
	public RegisterController(RegisterService registerService) {
		this.registerService = registerService;
	}

	@PostMapping("/register")
	public RegisterResponse register(
		@RequestBody RegisterRequest request
		)
	{
		RegisterResponse response = registerService.register(request.getUsername(), request.getEmail(), request.getPassword(), Role.STAFF);
		return response;
	}

	@GetMapping("/count")
	public String count() {
		return String.format("%d\n", registerService.count());
	}

	@GetMapping("/findById")
	public String findById(@RequestParam(value="id") long id) {
		User user = registerService.findById(id);
		return String.format("%s\n%s\n%s\n%s\n", user.getUsername(), user.getPassword(), user.getEmail(), user.getRole());
	}
}
