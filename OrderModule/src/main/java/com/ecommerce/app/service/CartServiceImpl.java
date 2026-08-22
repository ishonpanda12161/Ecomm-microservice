package com.ecommerce.app.service;

import com.ecommerce.app.config.HttpService.ProductService;
import com.ecommerce.app.config.HttpService.UserService;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.CartMapper;
import com.ecommerce.app.model.Cart;
import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.payload.CartDTO;
import com.ecommerce.app.payload.CartItemDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import com.ecommerce.app.repository.CartItemRepository;
import com.ecommerce.app.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService{

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ProductService productService;
    private final UserService userService;

    @Transactional
    @Override
    public Boolean addToCart(String keycloakId, CartItemDTO cartItemDTO) {

        ProductResponseDTO product = productService.getProduct(cartItemDTO.getProductId());

        if(product.getStockQuantity()<cartItemDTO.getQuantity())
        {
            return Boolean.FALSE;
        }

        UserResponseDTO user = userService.getUserByKeycloakId(keycloakId);

        Cart cart = getUserCart(user.getId());
        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart,cartItemDTO.getProductId());

        if(cartItem!=null)
        {
            if(product.getStockQuantity()<cartItem.getQuantity()+1) return Boolean.FALSE;

            cartItem.setQuantity(cartItem.getQuantity()+1);
            cartItemRepository.save(cartItem);
            return Boolean.TRUE;
        }

        cartItem = new CartItem(cartItemDTO.getProductId(),cartItemDTO.getQuantity());
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        return Boolean.TRUE;
    }

    @Transactional(readOnly = true)
    @Override
    public CartDTO getCart(String keycloakId) {

        UserResponseDTO user = userService.getUserByKeycloakId(keycloakId);

        Cart cart = getUserCart(user.getId());

        CartDTO cartDTO = cartMapper.toDTO(cart);
        cartDTO.setTotalPrice(calculateTotalPrice(cart));
        return cartDTO;
    }


    @Transactional
    @Override
    public Boolean updateCart(String keycloakId, String productId, String operation) {

        UserResponseDTO user = userService.getUserByKeycloakId(keycloakId);

        Cart cart = getUserCart(user.getId());
        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart,productId);
        if(cartItem==null)
        {
            return Boolean.FALSE;
        }
        ProductResponseDTO product = productService.getProduct(productId);

        int quantity = operation.equalsIgnoreCase("add")
                ? 1
                : -1;

        if(cartItem.getQuantity()+quantity==0)
        {
            cart.getCartItems().remove(cartItem);
            cartRepository.save(cart);
            return Boolean.TRUE;
        }

        if(product.getStockQuantity()<cartItem.getQuantity()+quantity)
        {
            return Boolean.FALSE;
        }
        cartItem.setQuantity(cartItem.getQuantity()+quantity);
        cartItemRepository.save(cartItem);
        return Boolean.TRUE;

    }


    @Transactional
    @Override
    public CartDTO deleteFromCart(String keycloakId, String productId) {

        UserResponseDTO user = userService.getUserByKeycloakId(keycloakId);

        Cart cart = getUserCart(user.getId());

        cart.getCartItems().removeIf(cartItem -> cartItem.getProductId().equals(productId));
        cart = cartRepository.saveAndFlush(cart);

        CartDTO cartDTO = cartMapper.toDTO(cart);
        cartDTO.setTotalPrice(calculateTotalPrice(cart));
        return cartDTO;
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

    private BigDecimal calculateTotalPrice(Cart cart)
    {

        Set<String> productIds = cart.getCartItems().stream().map(CartItem::getProductId).collect(Collectors.toSet());
        List<ProductResponseDTO> productsList = productService.getBatch(productIds);
        if(productsList==null || productsList.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        Map<String,ProductResponseDTO> products = productsList.stream().collect(Collectors.toMap(ProductResponseDTO::getId,p->p));

        BigDecimal total = cart.getCartItems().stream()
                .map( item ->
                {
                    ProductResponseDTO product = products.get(item.getProductId());
                    if(product!=null) return (product.getPrice().multiply(BigDecimal.ONE.subtract(product.getDiscount().divide(BigDecimal.valueOf(100)))))
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return BigDecimal.ZERO;
                }).reduce(BigDecimal.ZERO,BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

}
