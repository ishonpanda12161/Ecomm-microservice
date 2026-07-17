package com.ecommerce.app.service;

import com.ecommerce.app.payload.CartDTO;
import com.ecommerce.app.payload.CartItemDTO;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

public interface CartService {

    @Transactional
    Boolean addToCart(String userId, @Valid CartItemDTO cartItemDTO);

    @Transactional(readOnly = true)
    CartDTO getCart(String userId);

    @Transactional
    Boolean updateCart(String userId, String productId, String operation);

    @Transactional
    CartDTO deleteFromCart(String userId, String productId);
}
