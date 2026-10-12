package is.hi.store.repository;
import org.springframework.data.repository.Repository;
import is.hi.store.entity.Image;
import java.util.Optional;

public interface ImageRepository extends Repository<Image, Long> {
	Image save(Image image);
	Optional<Image> findBySlug(String slug);
}
