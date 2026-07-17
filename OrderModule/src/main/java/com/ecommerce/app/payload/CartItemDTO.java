package com.ecommerce.app.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CartItemDTO {

    @NotBlank(message = "Product Id cannot be blank.")
    private String productId;
    @Positive
    @NotNull(message = "Quantity Cannot be null.")
    private Integer quantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
