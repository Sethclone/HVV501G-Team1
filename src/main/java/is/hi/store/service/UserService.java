package is.hi.store.service;

import is.hi.store.repository.UserRepository;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.dto.UpdateAccountRequest;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public interface UserService {
	boolean findExistsByRole(Role role);
	User updateOwnAccount(String email, UpdateAccountRequest request);
}

@Service
class UserServiceImplementation implements UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public boolean findExistsByRole(Role role) {
		return userRepository.findExistsByRole(role);
	}

	// UC6. The email comes from the caller's token, never the body - a user can only ever edit themselves.
	public User updateOwnAccount(String email, UpdateAccountRequest request) {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new InvalidCredentialsException("Account no longer exists"));

		List<FieldErrorDetail> errors = new ArrayList<>();
		if (request.getName() != null && request.getName().isBlank()) {
			errors.add(new FieldErrorDetail("name", "Name must not be blank"));
		}
		if (request.getPassword() != null) {
			if (request.getPassword().length() < 8) {
				errors.add(new FieldErrorDetail("password", "Password must be at least 8 characters"));
			}
			// Re-auth so a stolen token can't lock the owner out - only an admin could recover the account.
			if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
				errors.add(new FieldErrorDetail("currentPassword", "Current password is missing or incorrect"));
			}
		}
		if (!errors.isEmpty()) {
			throw new InvalidRequestException(errors);
		}

		if (request.getName() != null) {
			user.setUsername(request.getName());
		}
		if (request.getPassword() != null) {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
		}
		return userRepository.save(user);
	}
}
