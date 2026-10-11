package is.hi.store.dto;

public class ReorderResponse {
	private long productId;
	private boolean newFlag;

	public ReorderResponse(long productId, boolean newFlag) {
		this.productId = productId;
		this.newFlag = newFlag;
	}
	public boolean getFlag() {return newFlag;}
	public long getProductId() {return productId;}
}
