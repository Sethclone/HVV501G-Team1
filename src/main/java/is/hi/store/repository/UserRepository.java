package is.hi.store.repository;
import org.springframework.data.repository.Repository;
import is.hi.store.entity.User;

public interface UserRepository extends Repository<User, Long> {
	User save(User user);
	User findById(long id);
	Long count();
}
