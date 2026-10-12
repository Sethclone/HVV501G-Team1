package is.hi.store.repository;
import org.springframework.data.repository.Repository;
import is.hi.store.entity.Image;

public interface ImageRepository extends Repository<Image, Long> {
	Image save(Image image);
	Image findBySlug(String slug);
}
