package com.ecommerce.app.controller;

import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.payload.OrderResponseDTO;
import com.ecommerce.app.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping()
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
            @RequestHeader("USER_ID") String userId
    )
    {
        return ResponseEntity.ok().body(orderService.getUserOrders(userId));
    }

    @PostMapping()
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestHeader("USER_ID") String userId
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(userId));
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Boolean> cancelOrder(
            @RequestHeader("USER_ID") String userId,
            @PathVariable String orderId
    )
    {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderService.cancelOrder(userId,orderId));
    }

    @PutMapping("/{status}/{orderId}")
    public ResponseEntity<Boolean> updateOrderStatus(
            @RequestHeader("USER_ID") String userId,
            @PathVariable String orderId,
            @PathVariable OrderStatus status
            )
    {
        return ResponseEntity.ok().body(orderService.updateOrderStatus(userId,orderId,status));
    }
}
