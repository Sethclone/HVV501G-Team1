package is.hi.store.service;

import is.hi.store.repository.UserRepository;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.dto.RegisterRequest;
import is.hi.store.dto.LoginRequest;
import is.hi.store.exception.EmailAlreadyExistsException;
import is.hi.store.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


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
