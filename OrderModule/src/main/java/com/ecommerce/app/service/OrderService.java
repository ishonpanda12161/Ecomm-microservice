package com.ecommerce.app.service;

import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.payload.OrderItemSearchResponseDTO;
import com.ecommerce.app.payload.OrderResponseDTO;
import com.ecommerce.app.payload.OrderSearchResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {
    @Transactional
    OrderResponseDTO createOrder(String keycloakId);
    @Transactional
    Boolean cancelOrder(String keycloakId, String orderId);

    @Transactional
    Boolean updateOrderStatus(String keycloakId, String orderId, OrderStatus status);

    @Transactional(readOnly = true)
    OrderSearchResponseDTO getUserOrders(String keycloakId,Integer pageNum,Integer pageSize,String sortBy,String sortDir);

    OrderSearchResponseDTO getAllOrders(Integer pageNum, Integer pageSize, String sortBy, String sortDir);

    OrderItemSearchResponseDTO getSellerOrders(String keycloakId, Integer pageNum, Integer pageSize, String sortBy, String sortDir);
}
