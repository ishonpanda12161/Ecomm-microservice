package com.ecommerce.app.repository;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartItem;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,String> {

    CartItem findByCartAndProductId(Cart cart, @NotBlank(message = "Product Id cannot be blank.") String productId);
}
