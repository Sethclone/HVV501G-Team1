package is.hi.store.dto;

import is.hi.store.entity.StockMovement.MovementType;
import is.hi.store.entity.StockMovement;
import java.time.Instant;

public class StockMovementResponse {
	private long id;
	private long productId;
	private long quantity;
	private MovementType type;
	private long performedBy;
	private Instant timestamp;
	private long newStockLevel;

	public StockMovementResponse(StockMovement stockMovement, long newStockLevel) {
		this.id = stockMovement.getId();
		this.productId = stockMovement.getProduct().getId();
		this.quantity = stockMovement.getQuantity();
		this.type = stockMovement.getType();
		this.performedBy = stockMovement.getPerformedBy().getId();
		this.timestamp = stockMovement.getTimestamp();
		this.newStockLevel = newStockLevel;
	}

	public void setId(long id) {this.id = id;}
	public void setProductId(long productId) {this.productId = productId;}
	public void setQuantity(long quantity) {this.quantity = quantity;}
	public void setType(MovementType type) {this.type = type;}
	public void setPerformedBy(long performedBy) {this.performedBy = performedBy;}
	public void setTimestamp(Instant timestamp) {this.timestamp = timestamp;}
	public void setNewStockLevel(long newStockLevel) {this.newStockLevel = newStockLevel;}

	public long getId() {return id;}
	public long getProductId() {return productId;}
	public long getQuantity() {return quantity;}
	public MovementType getType() {return type;}
	public long getPerformedBy() {return performedBy;}
	public Instant getTimestamp() {return timestamp;}
	public long getNewStockLevel() {return newStockLevel;}
}
