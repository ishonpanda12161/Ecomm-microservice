package com.ecommerce.app.service;

import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.payload.OrderResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {
    @Transactional
    OrderResponseDTO createOrder(String userId);
    @Transactional
    Boolean cancelOrder(String userId, String orderId);

    @Transactional
    Boolean updateOrderStatus(String userId, String orderId, OrderStatus status);

    @Transactional(readOnly = true)
    List<OrderResponseDTO> getUserOrders(String userId);
}
