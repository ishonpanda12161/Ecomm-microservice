package com.app.notificationmodule.payload;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderEvent {
    private String id;
    private String userId;
    private String email;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime createdAt;
}
