package com.ecommerce.app.mapper;

import com.ecommerce.app.model.User;
import com.ecommerce.app.payload.UserRequestDTO;
import com.ecommerce.app.payload.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = AddressMapper.class
)
public interface UserMapper {


    UserResponseDTO toDto(User user);

    List<UserResponseDTO> toDTOList(List<User> users);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    @Mapping(target = "updatedAt",ignore = true)
    User toEntity(UserRequestDTO userRequestDTO);
}
