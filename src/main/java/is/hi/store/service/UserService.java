package is.hi.store.service;

import is.hi.store.repository.UserRepository;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import is.hi.store.dto.AdminUpdateUserRequest;
import is.hi.store.dto.FieldErrorDetail;
import is.hi.store.dto.UpdateAccountRequest;
import is.hi.store.exception.EmailAlreadyExistsException;
import is.hi.store.exception.InvalidCredentialsException;
import is.hi.store.exception.InvalidRequestException;
import is.hi.store.exception.UserNotFoundException;
import is.hi.store.dto.UploadProfilePicRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public interface UserService {
	boolean findExistsByRole(Role role);
	User updateOwnAccount(String email, UpdateAccountRequest request);
	User uploadProfilePicture(String email, UploadProfilePicRequest request);
	User adminUpdateUser(String actorEmail, long id, AdminUpdateUserRequest request);
	String resetPassword(long id);
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
		validateName(request.getName(), errors);
		validatePassword(request.getPassword(), errors);
		if (request.getPassword() != null) {
			if (user.isMustChangePassword() && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
				// Otherwise the user could clear the flag and keep the temp password the admin saw.
				errors.add(new FieldErrorDetail("password", "New password must differ from the temporary password"));
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
			user.setMustChangePassword(false);	// temp password from an admin reset is replaced
		}
		return userRepository.save(user);
	}

	// UC7. actorEmail is the calling admin (from the token), used to stop self-demotion.
	public User adminUpdateUser(String actorEmail, long id, AdminUpdateUserRequest request) {
		User user = findUser(id);

		List<FieldErrorDetail> errors = new ArrayList<>();
		validateName(request.getName(), errors);
		String email = request.getEmail();
		validateEmail(email, errors);
		// Lockout guards. Not atomic: two admins demoting each other at the same moment can both pass.
		if (user.getRole() == Role.ADMIN && request.getRole() == Role.STAFF) {
			if (user.getEmail().equals(actorEmail)) {
				errors.add(new FieldErrorDetail("role", "Admins cannot demote themselves"));
			} else if (userRepository.countByRole(Role.ADMIN) <= 1) {
				errors.add(new FieldErrorDetail("role", "Cannot demote the last admin"));
			}
		}
		if (!errors.isEmpty()) {
			throw new InvalidRequestException(errors);
		}
		if (email != null && !email.equals(user.getEmail()) && userRepository.findByEmail(email).isPresent()) {
			throw new EmailAlreadyExistsException("Email already in use: " + email);
		}

		if (request.getName() != null) {
			user.setUsername(request.getName());
		}
		if (email != null) {
			user.setEmail(email);
		}
		if (request.getRole() != null) {
			user.setRole(request.getRole());
		}
		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException e) {	// email taken between the check above and the save
			throw new EmailAlreadyExistsException("Email already in use: " + email);
		}
	}

	// UC7. Closed system, a locked out user can only get back in through an admin. The temp password is
	// returned once and the user is forced to change it (see JwtAuthenticationFilter).
	public String resetPassword(long id) {
		User user = findUser(id);
		byte[] bytes = new byte[12];
		RANDOM.nextBytes(bytes);
		String temporaryPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);	// 16 chars
		user.setPassword(passwordEncoder.encode(temporaryPassword));
		user.setMustChangePassword(true);
		userRepository.save(user);
		return temporaryPassword;
	}

	private static final SecureRandom RANDOM = new SecureRandom();

	private User findUser(long id) {
		User user = userRepository.findById(id);
		if (user == null) {
			throw new UserNotFoundException(id);
		}
		return user;
	}

	// Shared with AuthService.register. A null value means "not being changed" and passes.
	static void validateName(String name, List<FieldErrorDetail> errors) {
		if (name != null && name.isBlank()) {
			errors.add(new FieldErrorDetail("name", "Name must not be blank"));
		} else if (name != null && name.length() > 255) {	// default column length
			errors.add(new FieldErrorDetail("name", "Name must be at most 255 characters"));
		}
	}

	static void validateEmail(String email, List<FieldErrorDetail> errors) {
		if (email != null && (email.length() > 255 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
			errors.add(new FieldErrorDetail("email", "Email must be a valid address of at most 255 characters"));
		}
	}

	static void validatePassword(String password, List<FieldErrorDetail> errors) {
		if (password != null && password.length() < 8) {
			errors.add(new FieldErrorDetail("password", "Password must be at least 8 characters"));
		} else if (password != null && password.getBytes(StandardCharsets.UTF_8).length > 72) {	// BCrypt hard limit, encode() throws past it
			errors.add(new FieldErrorDetail("password", "Password must be at most 72 bytes"));
		}
	}

	public User uploadProfilePicture(String email, UploadProfilePicRequest request) {
		String uploadDirectory = "uploads/";
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new InvalidCredentialsException("Account no longer Exists"));

		MultipartFile file = request.getImage();
        if (file != null) {
            try {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
                String timestamp = now.format(formatter);
                String fileName = timestamp + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDirectory).resolve(fileName);

                Files.copy(file.getInputStream(), filePath);
                String imageUrl = "/images/" + fileName;

				user.setProfilePictureURL(imageUrl);
				userRepository.save(user);
            } catch (IOException e) {
				//Need to change type of exception
                throw new RuntimeException("Uploading file failed", e);
            }
        }

		return user;
	}
}
