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


public interface UserService {
	boolean findExistsByRole(Role role);
}

@Service
class UserServiceImplementation implements UserService {
	@Autowired
	private UserRepository userRepository;

	public boolean findExistsByRole(Role role) {
		return userRepository.findExistsByRole(role);	
	}
}
