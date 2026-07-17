package com.ecommerce.app.mapper;


import com.ecommerce.app.model.Category;
import com.ecommerce.app.payload.CategoryRequestDTO;
import com.ecommerce.app.payload.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CategoryMapper {


    CategoryResponseDTO toDTO(Category category);
    List<CategoryResponseDTO> toDTOList(List<Category> categories);

    @Mapping(target = "products",ignore = true)
    @Mapping(target = "id",ignore = true)
    @Mapping(target = "createdAt",ignore = true)
    Category toEntity(CategoryRequestDTO categoryRequestDTO);

}
