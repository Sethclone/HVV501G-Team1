package is.hi.store.repository;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import is.hi.store.entity.User;
import is.hi.store.entity.User.Role;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
	User save(User user);
	User findById(long id);
	Long count();
	@Query("select count(p) = 1 from User p where p.role = ?1")
	boolean findExistsByRole(Role role);
	Optional<User> findByEmail(String email);
}
