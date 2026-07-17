package com.ecommerce.app.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

    private String id;
    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;

}