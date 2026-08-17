package com.ecommerce.app.service;

import com.ecommerce.app.config.HttpService.ProductService;
import com.ecommerce.app.config.HttpService.UserService;
import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.OrderMapper;
import com.ecommerce.app.model.*;
import com.ecommerce.app.payload.OrderResponseDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

//    @Value("${exchange.exchange.name}")
//    private String exchangeName;
//    @Value("${exchange.key.created}")
//    private String createdKey;
//    @Value("${exchange.key.cancelled}")
//    private String cancelledKey;
    @Value("${exchange.key.delivered}")
    private String deliveredKey;
    @Value("${exchange.key.shipped}")
    private String shippedKey;
    @Value("${exchange.key.confirmed}")
    private String confirmedKey;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;
    //private final RabbitTemplate rabbitTemplate;
    private final StreamBridge streamBridge;

    private List<OrderStatus> cancellableStatus = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.CREATED);
    @Transactional
    @Override
    public OrderResponseDTO createOrder(String userId) {

        Cart cart = getUserCart(userId);
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty())
        {
            throw new APIException("Cart is empty.","Empty", LocalDateTime.now());
        }

        Set<String> productIds = cart.getCartItems().stream().map(CartItem::getProductId).collect(Collectors.toSet());
        List<ProductResponseDTO> productResponseDTOS = productService.getBatch(productIds);
        Map<String,ProductResponseDTO> productMap = productResponseDTOS.stream().collect(Collectors.toMap(ProductResponseDTO::getId,product -> product));

        List<OrderItem> orderItems = cartItems.stream()
                .map(item ->
                {
                    ProductResponseDTO product = productMap.get(item.getProductId());
                    BigDecimal totalPrice = (product.getPrice().multiply(BigDecimal.ONE.subtract(product.getDiscount().divide(BigDecimal.valueOf(100)))))
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    productService.decreaseProductQuantity(item.getProductId(),item.getQuantity());
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
//        rabbitTemplate.convertAndSend(
//                exchangeName,
//                createdKey,
//                orderMapper.toOrderEvent(savedOrder)
//        );

        streamBridge.send("createOrder-out-0",orderMapper.toOrderEvent(savedOrder));

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

        if(!cancellableStatus.contains(order.getStatus()))
        {
            throw new APIException("Couldn't cancel order","ORDER cancellation",LocalDateTime.now());
        }

        int update = orderRepository.claimCancellation(orderId,userId,OrderStatus.CANCELLED,cancellableStatus,order.getVersion());
        if(update==0)
        {
            throw new APIException("Couldn't cancel order","ORDER cancellation",LocalDateTime.now());
        }

        order.getItems().forEach(orderItem -> {
            productService.increaseProductQuantity(orderItem.getProductId(),orderItem.getQuantity());
        });

        order = orderRepository.findByIdAndUserId(orderId,userId);

        streamBridge.send("cancelOrder-out-0",orderMapper.toOrderEvent(order));

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

        String key = switch (status) {
            case DELIVERED -> deliveredKey;
            case SHIPPED -> shippedKey;
            case CONFIRMED -> confirmedKey;
            default -> throw new ResourceNotFoundException(
                    "Routing Key",
                    "Key",
                    status.name(),
                    LocalDateTime.now()
            );
        };

        //publish event
//        rabbitTemplate.convertAndSend(
//                exchangeName,
//                key,
//                orderMapper.toOrderEvent(order)
//        );
        streamBridge.send("updateOrderStatus-out-0",
                MessageBuilder.withPayload(orderMapper.toOrderEvent(order)).setHeader("target_key",key).build());

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
