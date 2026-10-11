package is.hi.store.dto;

import java.math.BigDecimal;

public class ProductUpdateRequest {

    private String name;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;

    public ProductUpdateRequest() {}

    public ProductUpdateRequest(String name, String category, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
