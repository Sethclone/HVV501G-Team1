package is.hi.store.dto;

import is.hi.store.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;


public class ProductResponse {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private boolean reorderFlagged;
    private Instant createdAt;

    public ProductResponse(Product product){
        this.id = product.getId();
        this.name = product.getName();
        this.category = product.getCategory();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.imageUrl = product.getImageUrl();
        this.reorderFlagged = product.isReorderFlagged();
        this.createdAt = product.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public Integer getStockQuantity() { return stockQuantity; }
    public String getImageUrl() { return imageUrl; }
    public boolean isReorderFlagged() { return reorderFlagged; }
    public Instant getCreatedAt() { return createdAt; }

}
