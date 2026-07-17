package com.ecommerce.app.service;

import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.OrderMapper;
import com.ecommerce.app.model.*;
import com.ecommerce.app.payload.OrderResponseDTO;
import com.ecommerce.app.repository.CartRepository;
import com.ecommerce.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public OrderResponseDTO createOrder(String userId) {

        Cart cart = getUserCart(userId);
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty())
        {
            throw new APIException("Cart is empty.","Empty", LocalDateTime.now());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User","UserId",userId,LocalDateTime.now()));

        List<OrderItem> orderItems = cartItems.stream()
                .map(item ->
                {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",item.getProductId(), LocalDateTime.now()));
                    BigDecimal totalPrice = (product.getPrice().multiply(BigDecimal.ONE.subtract(product.getDiscount().divide(BigDecimal.valueOf(100)))))
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    product.setStockQuantity(product.getStockQuantity()-item.getQuantity());
                    return new OrderItem(product.getId(),item.getQuantity(),totalPrice);
                }).toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        Order order = new Order();
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        order.setUserId(userId);

        Order savedOrder = orderRepository.save(order);

        orderItems.forEach(item -> item.setOrder(savedOrder));
        savedOrder.setItems(orderItems);
        cart.getCartItems().clear();

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

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
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
