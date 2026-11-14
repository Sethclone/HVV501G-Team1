package is.hi.store.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import is.hi.store.util.order.*;

@Entity
@Table(name = "orders")
public class Order {

    public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, CANCELLED }

	//State design pattern. Required
	@Transient
	private OrderState state;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private Instant createdAt = Instant.now();

    public Order() {
		state = null;
		status = OrderStatus.PENDING;
	}

	@Transient
	private void initState() {	
		switch(status) {
			case OrderStatus.PENDING:
				this.state = new PendingState();
				break;
			case OrderStatus.CONFIRMED:
				this.state = new ConfirmedState();
				break;
			case OrderStatus.SHIPPED:
				this.state = new ShippedState();
				break;
			case OrderStatus.CANCELLED:
				this.state = new CancelledState();
				break;
		}
	}
	@Transient
	public void transition(OrderStatus targetStatus) {
		if(this.state == null) {
			initState();
		}

		state.transition(this, targetStatus);
	}

	@Transient
	public void setState(OrderState state) {
		this.state = state;	
	}

	@Transient
	public OrderState getState() {
		return state;	
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}


