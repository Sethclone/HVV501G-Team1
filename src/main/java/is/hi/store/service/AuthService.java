package is.hi.store.service;

import is.hi.store.repository.UserRepository;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.dto.LoginRequest;
import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.exception.EmailAlreadyExistsException;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public interface AuthService {
	User register(RegisterRequest request, Role role);
	User login(LoginRequest request);
}

@Service
class AuthServiceImplementation implements AuthService {
	@Autowired
	private UserRepository userRepository;
	@Autowired 
	private PasswordEncoder passwordEncoder;

	public User register(RegisterRequest request, Role role) {
		// All three are required here, so a missing one is checked as "" and fails its rule.
		List<FieldErrorDetail> errors = new ArrayList<>();
		UserServiceImplementation.validateName(Objects.requireNonNullElse(request.getUsername(), ""), errors);
		UserServiceImplementation.validateEmail(Objects.requireNonNullElse(request.getEmail(), ""), errors);
		UserServiceImplementation.validatePassword(Objects.requireNonNullElse(request.getPassword(), ""), errors);
		if (!errors.isEmpty()) {
			throw new InvalidRequestException(errors);
		}

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
		}

		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());

		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(role);

		User newUser = userRepository.save(user);
		return newUser;
	}

	public User login(LoginRequest request) {
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

		return user;
	}
}
