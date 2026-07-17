package com.ecommerce.app.service;

import com.ecommerce.app.exception.ResourceAlreadyExistsException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.CategoryMapper;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.payload.CategoryRequestDTO;
import com.ecommerce.app.payload.CategoryResponseDTO;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryRepository.findByName(categoryRequestDTO.getName());
        if(category!=null)
        {
            throw new ResourceAlreadyExistsException("Category","Name",categoryRequestDTO.getName(), LocalDateTime.now());
        }

        category = categoryMapper.toEntity(categoryRequestDTO);

        return categoryMapper.toDTO(categoryRepository.save(category));

    }

    @Override
    public CategoryResponseDTO getCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Category","Id",id,LocalDateTime.now()));

        return categoryMapper.toDTO(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponseDTO> getAllCategory() {
        List<Category> categories = categoryRepository.findAll();

        return categoryMapper.toDTOList(categories);
    }

    @Transactional
    @Override
    public CategoryResponseDTO updateCategory(String id, CategoryRequestDTO categoryRequestDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Category","Id",id,LocalDateTime.now()));
        category.setName(categoryRequestDTO.getName());
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public CategoryResponseDTO deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Category","Id",id,LocalDateTime.now()));
        Category uncategorized = getUncategorized();

        for(Product product : category.getProducts())
        {
            product.setCategory(uncategorized);
            productRepository.save(product);
        }

        categoryRepository.delete(category);
        return categoryMapper.toDTO(category);

    }

    private Category getUncategorized()
    {
        String name = "Uncategorized";
        Category category = categoryRepository.findByName(name);
        if(category==null)
        {
            Category uncategorized = new Category();
            uncategorized.setName("Uncategorized");
            return categoryRepository.save(uncategorized);
        }
        return category;
    }
}
