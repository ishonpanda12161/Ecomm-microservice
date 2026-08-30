package com.ecommerce.app.service;

import com.ecommerce.app.config.HttpService.ProductService;
import com.ecommerce.app.config.HttpService.UserService;
import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.OrderItemMapper;
import com.ecommerce.app.mapper.OrderMapper;
import com.ecommerce.app.model.*;
import com.ecommerce.app.payload.*;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.OrderItemRepository;
import com.ecommerce.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    @Value("${exchange.key.completed}")
    private String completedKey;
    @Value("${exchange.key.shipped}")
    private String shippedKey;
    @Value("${exchange.key.cancelled}")
    private String cancelledKey;
    @Value("${exchange.key.confirmed}")
    private String confirmedKey;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserService userService;
    private final OrderItemMapper orderItemMapper;
    //private final RabbitTemplate rabbitTemplate;
    private final StreamBridge streamBridge;
    private List<OrderStatus> cancellableStatus = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.CREATED, OrderStatus.SHIPPED);


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
                    return new OrderItem(product.getId(),item.getQuantity(),totalPrice,product.getSellerId());
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

        OrderEvent event = orderMapper.toOrderEvent(savedOrder);
        UserResponseDTO userResponseDTO = getUser(userId);
        event.setEmail(userResponseDTO.getEmail());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        streamBridge.send("createOrder-out-0",event);
                    }
                }
        );

        return orderMapper.toDTO(savedOrder);
    }

    @Transactional
    @Override
    public Boolean cancelOrder(String userId, String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order","ID",orderId,LocalDateTime.now()));
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
            orderItem.setStatus(OrderStatus.CANCELLED);
            productService.increaseProductQuantity(orderItem.getProductId(),orderItem.getQuantity());
        });

        orderItemRepository.saveAll(order.getItems());

        order = orderRepository.findByIdAndUserId(orderId,userId);
        OrderEvent event = orderMapper.toOrderEvent(order);
        UserResponseDTO userResponseDTO = getUser(userId);
        event.setEmail(userResponseDTO.getEmail());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        streamBridge.send("cancelOrder-out-0",event);
                    }
                }
        );

        return Boolean.TRUE;
    }

    @Transactional
    @Override
    public Boolean updateOrderStatus(String userId, String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order","ID",orderId,LocalDateTime.now()));

        if(order==null)
        {
            return Boolean.FALSE;
        }
        if(order.getStatus().equals(OrderStatus.COMPLETED))
        {
            return Boolean.FALSE;
        }

        String key = switch (status) {
            case COMPLETED -> completedKey;
            case SHIPPED -> shippedKey;
            case CONFIRMED -> confirmedKey;
            default -> throw new ResourceNotFoundException(
                    "Routing Key",
                    "Key",
                    status.name(),
                    LocalDateTime.now()
            );
        };
        order.setStatus(status);
        orderRepository.save(order);

        OrderEvent event = orderMapper.toOrderEvent(order);
        UserResponseDTO userResponseDTO = getUser(userId);
        event.setEmail(userResponseDTO.getEmail());

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        streamBridge.send("updateOrderStatus-out-0",
                                MessageBuilder.withPayload(event).setHeader("target_key",key).build());
                    }
                }
        );


        return Boolean.TRUE;
    }

    @Transactional(readOnly = true)
    @Override
    public OrderSearchResponseDTO getUserOrders(String userId,Integer pageNum, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortByAndOrder);
        Page<Order> orderPage = orderRepository.findByUserId(userId,pageable);
        List<OrderResponseDTO> orderResponseDTOS = orderMapper.toDTOList(orderPage.getContent());
        if (orderResponseDTOS.isEmpty()) {
            throw new APIException("No orders found.", "Content", LocalDateTime.now());
        }

        OrderSearchResponseDTO response = new OrderSearchResponseDTO();
        response.setOrderResponseDTOS(orderResponseDTOS);
        response.setLastPage(orderPage.isLast());
        response.setPageNum(orderPage.getNumber());
        response.setPageSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());

        return response;
    }

    @Override
    public OrderSearchResponseDTO getAllOrders(Integer pageNum, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortByAndOrder);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<OrderResponseDTO> orderResponseDTOS = orderMapper.toDTOList(orderPage.getContent());
        if (orderResponseDTOS.isEmpty()) {
            throw new APIException("No orders found.", "Content", LocalDateTime.now());
        }

        OrderSearchResponseDTO response = new OrderSearchResponseDTO();
        response.setOrderResponseDTOS(orderResponseDTOS);
        response.setLastPage(orderPage.isLast());
        response.setPageNum(orderPage.getNumber());
        response.setPageSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());

        return response;
    }

    @Override
    public OrderItemSearchResponseDTO getSellerOrders(String keycloakId,Integer pageNum, Integer pageSize, String sortBy, String sortDir) {

        UserResponseDTO user = getUser(keycloakId);

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortByAndOrder);

        Page<OrderItem> orderItemPage = orderItemRepository.findBySellerId(user.getKeycloakId(),pageable);
        List<OrderItem> orderItemResponseDTOS = orderItemMapper.toList(orderItemPage.getContent());
        if (orderItemResponseDTOS.isEmpty()) {
            throw new APIException("No orders found.", "Content", LocalDateTime.now());
        }

        OrderItemSearchResponseDTO response = new OrderItemSearchResponseDTO();
        response.setOrderItems(orderItemResponseDTOS);
        response.setLastPage(orderItemPage.isLast());
        response.setPageNum(orderItemPage.getNumber());
        response.setPageSize(orderItemPage.getSize());
        response.setTotalElements(orderItemPage.getTotalElements());
        response.setTotalPages(orderItemPage.getTotalPages());

        return response;
    }

    @Transactional
    @Override
    public Boolean updateOrderItemStatus(@Nullable String keycloakId, String orderItemId, OrderStatus status) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order Item","ID",orderItemId,LocalDateTime.now()));

        if(orderItem==null)
        {
            return Boolean.FALSE;
        }

        orderItem.setStatus(status);
        orderItemRepository.save(orderItem);

        return Boolean.TRUE;
    }


    private Cart getUserCart(String keycloakId)
    {
        UserResponseDTO user = getUser(keycloakId);
        Cart cart = cartRepository.findByUserId(user.getId());
        if(cart==null)
        {
            Cart newCart = new Cart();
            newCart.setUserId(user.getId());
            return cartRepository.save(newCart);
        }
        return cart;
    }

    private UserResponseDTO getUser(String keycloakId)
    {
        UserResponseDTO user = userService.getUserByKeycloakId(keycloakId);
        return user;
    }

}
