package is.hi.store.dto;
import is.hi.store.entity.StockMovement.MovementType;

public class StockMovementRequest {
	private int quantity;
	private MovementType type;

	public void setQuantity(int quantity) {this.quantity = quantity;}
	public int getQuantity() {return quantity;}
	public void setType(MovementType type) {this.type = type;}
	public MovementType getType() {return type;}
}
