package com.ecommerce.app.mapper;

import com.ecommerce.app.model.Address;
import com.ecommerce.app.payload.AddressDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDTO toDTO(Address address);

    List<AddressDTO> toDTOList(List<Address> addresses);

    @Mapping(target = "id",ignore = true)
    @Mapping(target = "userId",ignore = true)
    Address toEntity(AddressDTO addressDTO);
}
