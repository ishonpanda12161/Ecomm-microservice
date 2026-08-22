package com.ecommerce.app.mapper;

import com.ecommerce.app.model.OrderItem;
import com.ecommerce.app.payload.OrderItemDTO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemDTO toDTO(OrderItem orderItem);

    List<OrderItem> toList(List<OrderItem> orderItems);

    List<OrderItemDTO> toDTOList(List<OrderItem> orderItems);


}
