package com.ecommerce.app.payload;

import com.ecommerce.app.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {

    private String id;
    private String sellerId;
    private String productId;
    private OrderStatus status;
    private Integer quantity;
    private BigDecimal totalPrice;

}