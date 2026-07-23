package com.ecommerce.app.service;

import com.ecommerce.app.payload.ProductRequestDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import com.ecommerce.app.payload.ProductSearchResponseDTO;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductService {

    List<ProductResponseDTO> getAllProducts();

    @Transactional(readOnly = true)
    ProductSearchResponseDTO searchProducts(Integer pageNum, Integer pageSize, String sortBy, String sortDir);

    ProductResponseDTO createProduct(@Valid ProductRequestDTO productRequestDTO,String categoryId);

    @Transactional
    ProductResponseDTO updateProduct(@Valid ProductRequestDTO productRequestDTO, String id);

    @Transactional
    ProductResponseDTO deleteProduct(String id);

    @Transactional(readOnly = true)
    ProductSearchResponseDTO searchProductsByKeyword(String keyword,Integer pageNum, Integer pageSize, String sortBy, String sortDir);

    @Transactional(readOnly = true)
    ProductSearchResponseDTO searchProductsByCategory(String category,Integer pageNum, Integer pageSize, String sortBy, String sortDir);

    ProductResponseDTO getProductById(String id);

    @Transactional
    void updateProductQuantity(String id, Integer quantity);
}
