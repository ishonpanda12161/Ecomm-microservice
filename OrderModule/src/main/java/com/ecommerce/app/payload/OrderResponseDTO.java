package com.ecommerce.app.payload;

import com.ecommerce.app.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private String id;
    private String userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemDTO> orderItemDTOs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
