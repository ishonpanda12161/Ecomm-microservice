package com.ecommerce.app.payload;

import com.ecommerce.app.model.OrderStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderEvent {

    private String id;
    private String userId;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private List<OrderItemDTO> orderItems;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
