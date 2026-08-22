package com.ecommerce.app.service;

import com.ecommerce.app.payload.CartDTO;
import com.ecommerce.app.payload.CartItemDTO;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

public interface CartService {

    @Transactional
    Boolean addToCart(String keycloakId, @Valid CartItemDTO cartItemDTO);

    @Transactional(readOnly = true)
    CartDTO getCart(String keycloakId);

    @Transactional
    Boolean updateCart(String keycloakId, String productId, String operation);

    @Transactional
    CartDTO deleteFromCart(String keycloakId, String productId);
}
