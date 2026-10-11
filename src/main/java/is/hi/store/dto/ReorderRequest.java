package is.hi.store.dto;

public class ReorderRequest {
	private Boolean flagged;	// boxed so a missing field is null (400), not a silent false

	public ReorderRequest() {}
	public Boolean getFlagged() {return flagged;}
	public void setFlagged(Boolean flagged) {this.flagged = flagged;}
}
