package is.hi.store.entity;
import jakarta.persistence.*;
import java.time.Instant;
import is.hi.store.entity.Product;
import is.hi.store.entity.User;

@Entity
@Table(name="stock_movements")
public class StockMovement {
	public enum MovementType{ADD, REMOVE};
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	@ManyToOne
	@JoinColumn(name="product")
	private Product product;
	@Column(name="quantity")
	private int quantity;
	@Enumerated(EnumType.STRING)
	@Column(name="type")
	private MovementType type;
	@ManyToOne
	@JoinColumn(name="performedBy")
	private User performedBy;
	@Column(name="timestamp")
	private Instant timestamp;

	public StockMovement() {timestamp = Instant.now();}

	public void setProduct(Product product) {this.product = product;}
	public void setQuantity(int quantity) {this.quantity = quantity;}
	public void setType(MovementType type) {this.type = type;}
	public void setPerformedBy(User performedBy) {this.performedBy = performedBy;}

	public long getId() {return id;}
	public Product getProduct() {return product;}
	public int getQuantity() {return quantity;}
	public MovementType getType() {return type;}
	public User getPerformedBy() {return performedBy;}
	public Instant getTimestamp() {return timestamp;}
}
