package is.hi.store.controller;

import is.hi.store.config.JwtService;
import is.hi.store.service.AuthService;
import is.hi.store.service.UserService;
import is.hi.store.dto.LoginRequest;
import is.hi.store.dto.LoginResponse;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.dto.RegisterResponse;
import is.hi.store.dto.UserSummary;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final JwtService jwtService;
	private final UserService userService;

	public AuthController(AuthService authService, JwtService jwtService, UserService userService) {
		this.authService = authService;
		this.jwtService = jwtService;
		this.userService = userService;
	}	

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request)
	{
		User newUser = authService.register(request, Role.STAFF);
		return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterResponse(newUser));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

		User user = authService.login(request);
		String token = jwtService.generateToken(user);
		Instant expiresAt = jwtService.extractExpiration(token);

		return ResponseEntity.ok(new LoginResponse(token, expiresAt, new UserSummary(user)));
	}

	@Bean 
	public CommandLineRunner commandLineRunner() {
		return args -> {
			if(!userService.findExistsByRole(Role.ADMIN))
				authService.register(new RegisterRequest("root", "root@root.com", "password"), Role.ADMIN);
		};
	}
}
