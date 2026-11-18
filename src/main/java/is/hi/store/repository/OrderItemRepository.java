package is.hi.store.repository;

import is.hi.store.entity.OrderItem;
import is.hi.store.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    boolean existsByProduct(Product user);
}
