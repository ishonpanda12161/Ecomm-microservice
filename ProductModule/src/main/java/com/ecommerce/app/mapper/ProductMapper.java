package com.ecommerce.app.mapper;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.payload.ProductRequestDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryName",source = "category.name")
    ProductResponseDTO toDTO(Product product);

    List<ProductResponseDTO> toDTOList(List<Product> products);

    @Mapping(target = "sellerId",ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequestDTO productRequestDTO);
}
