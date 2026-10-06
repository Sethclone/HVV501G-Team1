package is.hi.store.entity;
import jakarta.persistence.*;
import java.time.Instant;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;

@Entity
@Table(name="stock_movement")
public class StockMovement {
	public enum MovementType{ADD, REMOVE};
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@ManyToOne
	private Product product;
	@Column(name="quantity")
	private long quantity;
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private MovementType type;
	@ManyToOne
	private User performedBy;
	@Column(name="timestamp")
	private Instant timestamp;

	public StockMovement() {timestamp = Instant.now();}

	public Product getUsername() {return product;}
	public long getPassword() {return quantity;}
	public MovementType getEmail() {return type;}
	public User getRole() {return performedBy;}
	public Instant getId() {return timestamp;}
}
