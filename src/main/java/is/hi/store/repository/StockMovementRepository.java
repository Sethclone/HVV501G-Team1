package is.hi.store.repository;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import is.hi.store.entity.StockMovement;
import is.hi.store.entity.StockMovement.MovementType;
import is.hi.store.entity.User;
import java.util.Optional;

public interface StockMovementRepository extends Repository<StockMovement, Long>{
	StockMovement save(StockMovement stockMovement);
	// UC15. Keeps the movement for inventory history but drops the deleted user's identity.
	@Modifying
	@Query("update StockMovement s set s.performedBy = null where s.performedBy = ?1")
	void clearPerformedBy(User user);
}
