package is.hi.store.service;

import is.hi.store.repository.UserRepository;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.dto.RegisterResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.boot.CommandLineRunner;


public interface RegisterService {
	RegisterResponse register(String username, String email, String password, Role role);
	long count();
	User findById(long id);
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
		User user = new User();
		user.setUsername(username);
		user.setEmail(email);

		user.setPassword(passwordEncoder.encode(password));
		user.setRole(role);

		User newUser = userRepository.save(user);
		System.out.println(newUser.getUsername());
		return new RegisterResponse(newUser.getUsername(), newUser.getPassword(), newUser.getEmail());
	}

	public long count() {
		return userRepository.count();
	}

	public User findById(long id) {
		return userRepository.findById(id);
	}

	@Bean 
	public CommandLineRunner commandLineRunner() {
		return args -> {
			if(!userRepository.findExistsByRole(Role.ADMIN)) {
				User user = new User();
				user.setUsername("root");
				user.setEmail("root@root.com");
				user.setPassword(passwordEncoder.encode("password"));
				user.setRole(Role.ADMIN);
				userRepository.save(user);
			}
		};
	}
}
