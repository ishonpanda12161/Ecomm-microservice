package com.ecommerce.app.service;


import com.ecommerce.app.payload.CategoryRequestDTO;
import com.ecommerce.app.payload.CategoryResponseDTO;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO createCategory(@Valid CategoryRequestDTO categoryRequestDTO);

    CategoryResponseDTO getCategory(String id);

    @Transactional(readOnly = true)
    List<CategoryResponseDTO> getAllCategory();

    @Transactional
    CategoryResponseDTO updateCategory(String id, @Valid CategoryRequestDTO categoryRequestDTO);

    @Transactional
    CategoryResponseDTO deleteCategory(String id);
}

