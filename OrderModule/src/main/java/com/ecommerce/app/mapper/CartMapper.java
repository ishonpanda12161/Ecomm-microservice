package com.ecommerce.app.mapper;

import com.ecommerce.app.model.Cart;
import com.ecommerce.app.payload.CartDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = CartItemMapper.class
)
public interface CartMapper {

    @Mapping(source = "cartItems",target = "cartItemDTOs")
    @Mapping(target = "totalPrice",ignore = true)
    CartDTO toDTO(Cart cart);


}
