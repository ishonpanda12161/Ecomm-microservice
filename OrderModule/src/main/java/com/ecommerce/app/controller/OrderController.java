package com.ecommerce.app.controller;

import com.ecommerce.app.config.AppConfig;
import com.ecommerce.app.model.OrderStatus;
import com.ecommerce.app.payload.OrderItemSearchResponseDTO;
import com.ecommerce.app.payload.OrderResponseDTO;
import com.ecommerce.app.payload.OrderSearchResponseDTO;
import com.ecommerce.app.service.OrderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping()
    public ResponseEntity<OrderSearchResponseDTO> getUserOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
            @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_ORDERS_BY,required = false) String sortBy,
            @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    )
    {
        return ResponseEntity.ok().body(orderService.getUserOrders(jwt.getSubject(),pageNum,pageSize,sortBy,sortDir));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<OrderSearchResponseDTO> getAllOrders(
            @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
            @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_ORDERS_BY,required = false) String sortBy,
            @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    )
    {
        return ResponseEntity.ok().body(orderService.getAllOrders(pageNum,pageSize,sortBy,sortDir));
    }

    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    @GetMapping("/seller/all")
    public ResponseEntity<OrderItemSearchResponseDTO> getSellerOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
            @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_ORDERS_BY,required = false) String sortBy,
            @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    )
    {
        return ResponseEntity.ok().body(orderService.getSellerOrders(jwt.getSubject(),pageNum,pageSize,sortBy,sortDir));
    }


    @PostMapping()
    public ResponseEntity<OrderResponseDTO> createOrder(
            @AuthenticationPrincipal Jwt jwt
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(jwt.getSubject()));
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<Boolean> cancelOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String orderId
    )
    {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(orderService.cancelOrder(jwt.getSubject(),orderId));
    }

    @PutMapping("/{status}/{orderId}")
    public ResponseEntity<Boolean> updateOrderStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String orderId,
            @PathVariable OrderStatus status
            )
    {
        return ResponseEntity.ok().body(orderService.updateOrderStatus(jwt.getSubject(),orderId,status));
    }

    @PutMapping("/item/{status}/{orderItemId}")
    public ResponseEntity<Boolean> updateOrderItemStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String orderItemId,
            @PathVariable OrderStatus status
    )
    {
        return ResponseEntity.ok().body(orderService.updateOrderItemStatus(jwt.getSubject(),orderItemId,status));
    }
}
