package com.ecommerce.app.controller;


import com.ecommerce.app.payload.CartDTO;
import com.ecommerce.app.payload.CartItemDTO;
import com.ecommerce.app.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping()
    public ResponseEntity<Boolean> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CartItemDTO cartItemDTO
    )
    {
        Boolean response = cartService.addToCart(jwt.getSubject(),cartItemDTO);
        if(!response)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping()
    public ResponseEntity<CartDTO> getCart(
            @AuthenticationPrincipal Jwt jwt
    )
    {
        return ResponseEntity.ok().body(cartService.getCart(jwt.getSubject()));
    }

    @PutMapping("/{productId}/{operation}")
    public ResponseEntity<Boolean> updateCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId,
            @PathVariable String operation
    )
    {
        return ResponseEntity.ok().body(cartService.updateCart(jwt.getSubject(),productId,operation));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CartDTO> deleteFromCart(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId
    )
    {
        cartService.deleteFromCart(jwt.getSubject(),productId);
        return ResponseEntity.noContent().build();
    }

}
