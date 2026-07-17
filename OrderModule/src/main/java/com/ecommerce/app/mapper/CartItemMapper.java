package com.ecommerce.app.mapper;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.payload.CartItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    CartItem toEntity(CartItemDTO cartItemDTO);

    CartItemDTO toDTO(CartItem cartItem);

    List<CartItemDTO> toDTOList(List<CartItem> cartItems);


}
