package com.ecommerce.app.mapper;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.payload.OrderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = OrderItemMapper.class
)
public interface OrderMapper {

    @Mapping(source = "items",target = "orderItemDTOs")
    OrderResponseDTO toDTO(Order order);

    List<OrderResponseDTO> toDTOList(List<Order> orders);

}
