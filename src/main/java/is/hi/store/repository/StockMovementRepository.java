package is.hi.store.repository;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Query;
import is.hi.store.entity.StockMovement;
import is.hi.store.entity.StockMovement.MovementType;
import java.util.Optional;

public interface StockMovementRepository extends Repository<StockMovement, Long>{
	StockMovement save(StockMovement stockMovement);
}
