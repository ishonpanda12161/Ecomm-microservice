package com.ecommerce.app.controller;


import com.ecommerce.app.payload.CartDTO;
import com.ecommerce.app.payload.CartItemDTO;
import com.ecommerce.app.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping()
    public ResponseEntity<Boolean> addToCart(
            @RequestHeader("USER_ID") String userId,
            @RequestBody @Valid CartItemDTO cartItemDTO
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(userId,cartItemDTO));
    }

    @GetMapping()
    public ResponseEntity<CartDTO> getCart(
            @RequestHeader("USER_ID") String userId
    )
    {
        return ResponseEntity.ok().body(cartService.getCart(userId));
    }

    @PutMapping("/{productId}/{operation}")
    public ResponseEntity<Boolean> updateCart(
            @RequestHeader("USER_ID") String userId,
            @PathVariable String productId,
            @PathVariable String operation
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.updateCart(userId,productId,operation));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<CartDTO> deleteFromCart(
            @RequestHeader("USER_ID") String userId,
            @PathVariable String productId
    )
    {
        cartService.deleteFromCart(userId,productId);
        return ResponseEntity.noContent().build();
    }

}
