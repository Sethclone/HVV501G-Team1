package is.hi.store.dto;

public class ReassignOrderRequest {
    private Long userId;

    public ReassignOrderRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}