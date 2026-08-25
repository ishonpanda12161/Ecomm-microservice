package com.ecommerce.app.payload;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Product Name cannot be blank.")
    @Size(min = 3,message = "Name must be at least 3 characters.")
    private String name;

    private String categoryName;

    private String sellerId;

    private String description;

    @NotNull(message = "Product Price cannot be null.")
    @Positive
    private BigDecimal price;

    private BigDecimal discount;

    @Min(value = 0,message = "Stock cannot be less than 0.")
    private Integer stockQuantity;

    private String imageUrl;

    private Boolean active = true;
}
