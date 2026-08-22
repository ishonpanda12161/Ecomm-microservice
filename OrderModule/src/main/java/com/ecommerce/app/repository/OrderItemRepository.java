package com.ecommerce.app.repository;

import com.ecommerce.app.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem,String> {

    Page<OrderItem> findBySellerId(String id,
                                   Pageable pageable);
}
