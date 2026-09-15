package is.hi.store.controller;

import is.hi.store.config.JwtService;
import is.hi.store.dto.LoginRequest;
import is.hi.store.dto.LoginResponse;
import is.hi.store.dto.UserSummary;
import is.hi.store.entity.User;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		// Same error either way (bad email vs bad password) - don't let a caller use this
		// endpoint to enumerate which registered emails exist.
		if (request.getEmail() == null || request.getPassword() == null) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		User user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new InvalidCredentialsException("Invalid email or password");
		}

		String token = jwtService.generateToken(user);
		Instant expiresAt = jwtService.extractExpiration(token);

		return ResponseEntity.ok(new LoginResponse(token, expiresAt, new UserSummary(user)));
	}
}
