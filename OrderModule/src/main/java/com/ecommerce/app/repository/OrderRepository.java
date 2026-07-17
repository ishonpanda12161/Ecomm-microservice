package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {
    Order findByIdAndUserId(String orderId, String userId);
    List<Order> findByUserId(String userId);
}
