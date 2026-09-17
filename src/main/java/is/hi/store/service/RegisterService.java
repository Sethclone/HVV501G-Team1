package is.hi.store.service;

import is.hi.store.repository.UserRepository;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.dto.RegisterResponse;
import is.hi.store.exception.EmailAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


public interface RegisterService {
	RegisterResponse register(String username, String email, String password, Role role);
	long count();
	User findById(long id);
	boolean findExistsByRole(Role role);
}

@Service
class RegisterServiceImplementation implements RegisterService {
	@Autowired
	private UserRepository userRepository;
	@Autowired 
	private PasswordEncoder passwordEncoder;

	public RegisterResponse register(
	String username,
	String email,
	String password,
	Role role) {
		if (userRepository.findByEmail(email).isPresent()) {
			throw new EmailAlreadyExistsException("Email already in use: " + email);
		}

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);

		user.setPassword(passwordEncoder.encode(password));
		user.setRole(role);

		User newUser = userRepository.save(user);
		return new RegisterResponse(newUser);
	}

	public long count() {
		return userRepository.count();
	}

	public User findById(long id) {
		return userRepository.findById(id);
	}

	public boolean findExistsByRole(Role role) {
		return userRepository.findExistsByRole(role);	
	}
}
