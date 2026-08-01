package com.ecommerce.app.service;

import com.ecommerce.app.config.HttpService.ProductService;
import com.ecommerce.app.config.HttpService.UserService;
import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.mapper.OrderMapper;
import com.ecommerce.app.model.*;
import com.ecommerce.app.payload.OrderResponseDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.key.created}")
    private String createdKey;
    @Value("${rabbitmq.key.cancelled}")
    private String cancelledKey;
    @Value("${rabbitmq.key.delivered}")
    private String deliveredKey;
    @Value("${rabbitmq.key.shipped}")
    private String shippedKey;
    @Value("${rabbitmq.key.confirmed}")
    private String confirmedKey;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public OrderResponseDTO createOrder(String userId) {

        Cart cart = getUserCart(userId);
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty())
        {
            throw new APIException("Cart is empty.","Empty", LocalDateTime.now());
        }

        UserResponseDTO user = userService.getUser(userId);

        List<OrderItem> orderItems = cartItems.stream()
                .map(item ->
                {
                    ProductResponseDTO product = productService.getProduct(item.getProductId());
                    // N+1 optimize fetching product ^
                    BigDecimal totalPrice = (product.getPrice().multiply(BigDecimal.ONE.subtract(product.getDiscount().divide(BigDecimal.valueOf(100)))))
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    productService.updateProductQuantity(item.getProductId(),product.getStockQuantity()-item.getQuantity());
                    return new OrderItem(product.getId(),item.getQuantity(),totalPrice);
                }).toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        Order order = new Order();
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        order.setUserId(userId);

        Order savedOrder = orderRepository.saveAndFlush(order);

        orderItems.forEach(item ->
        {
            item.setOrder(savedOrder);
            savedOrder.getItems().add(item);
        });
        cart.getCartItems().clear();

        //publish event
        rabbitTemplate.convertAndSend(
                exchangeName,
                createdKey,
                orderMapper.toOrderEvent(savedOrder)
        );

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional
    @Override
    public Boolean cancelOrder(String userId, String orderId) {

        Order order = orderRepository.findByIdAndUserId(orderId,userId);
        if(order==null)
        {
            return Boolean.FALSE;
        }
        // Re-Stock products
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        //publish event
        rabbitTemplate.convertAndSend(
                exchangeName,
                cancelledKey,
                orderMapper.toOrderEvent(order)
        );

        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public Boolean updateOrderStatus(String userId, String orderId, OrderStatus status) {
        Order order = orderRepository.findByIdAndUserId(orderId,userId);
        if(order==null)
        {
            return Boolean.FALSE;
        }

        order.setStatus(status);
        orderRepository.save(order);

        String key;

        switch (status){
            case DELIVERED -> key=deliveredKey;
            case SHIPPED -> key=shippedKey;
            case CONFIRMED -> key=confirmedKey;
            case CANCELLED -> key=cancelledKey;
            default -> key=confirmedKey;
        }

        //publish event
        rabbitTemplate.convertAndSend(
                exchangeName,
                key,
                orderMapper.toOrderEvent(order)
        );
        return Boolean.TRUE;
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDTO> getUserOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orderMapper.toDTOList(orders);
    }


    private Cart getUserCart(String userId)
    {
        Cart cart = cartRepository.findByUserId(userId);
        if(cart==null)
        {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            return cartRepository.save(newCart);
        }
        return cart;
    }


}
