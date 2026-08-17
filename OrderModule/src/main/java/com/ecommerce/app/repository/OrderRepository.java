package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {
    Order findByIdAndUserId(String orderId, String userId);
    List<Order> findByUserId(String userId);

    @Modifying
    @Query(
        """
            update Order o
            set o.status = :status , o.version = o.version+1
            where o.id = :id
            and o.userId = :userId
            and o.status IN :cancellable
            and o.version = :version
        """
    )
    int claimCancellation(
            String id,
            String userId,
            OrderStatus status,
            List<OrderStatus> cancellable,
            Long version
    );
}
