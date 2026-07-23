package com.ecommerce.app.payload;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDTO {

    private String id;
    private String sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discount;
    private Integer stockQuantity;
    private String categoryName;
    private String imageUrl;
    private Boolean active;

}
