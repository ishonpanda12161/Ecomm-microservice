package com.app.notificationmodule.payload;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private String id;
    private String productId;
    private Integer quantity;
    private BigDecimal totalPrice;

}