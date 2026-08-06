package com.ecommerce.app.service;

import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.exception.ResourceAlreadyExistsException;
import com.ecommerce.app.exception.ResourceNotFoundException;
import com.ecommerce.app.mapper.ProductMapper;
import com.ecommerce.app.model.Category;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.payload.ProductRequestDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import com.ecommerce.app.payload.ProductSearchResponseDTO;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return productMapper.toDTOList(products);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductSearchResponseDTO searchProducts(Integer pageNum, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageable);

        List<ProductResponseDTO> productResponseDTOS = productMapper.toDTOList(productPage.getContent());
        if (productResponseDTOS.isEmpty()) {
            throw new APIException("No products found.", "Content", LocalDateTime.now());
        }

        ProductSearchResponseDTO response = new ProductSearchResponseDTO();
        response.setProductResponseDTOS(productResponseDTOS);
        response.setLastPage(productPage.isLast());
        response.setPageNum(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());

        return response;

    }

    @Transactional(readOnly = true)
    @Override
    public ProductSearchResponseDTO searchProductsByKeyword(String keyword,Integer pageNum, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword,keyword,pageable);

        List<ProductResponseDTO> productResponseDTOS = productMapper.toDTOList(productPage.getContent());
        if (productResponseDTOS.isEmpty()) {
            throw new APIException("No products found with keyword: "+keyword, "Content", LocalDateTime.now());
        }

        ProductSearchResponseDTO response = new ProductSearchResponseDTO();
        response.setProductResponseDTOS(productResponseDTOS);
        response.setLastPage(productPage.isLast());
        response.setPageNum(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());

        return response;

    }

    @Transactional(readOnly = true)
    @Override
    public ProductSearchResponseDTO searchProductsByCategory(String category,Integer pageNum, Integer pageSize, String sortBy, String sortDir) {

        Sort sortByAndOrder = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategory_Name(category,pageable);

        List<ProductResponseDTO> productResponseDTOS = productMapper.toDTOList(productPage.getContent());
        if (productResponseDTOS.isEmpty()) {
            throw new APIException("No products found with category: "+category, "Content", LocalDateTime.now());
        }

        ProductSearchResponseDTO response = new ProductSearchResponseDTO();
        response.setProductResponseDTOS(productResponseDTOS);
        response.setLastPage(productPage.isLast());
        response.setPageNum(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());

        return response;

    }

    @Override
    public ProductResponseDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",id,LocalDateTime.now()));
        return productMapper.toDTO(product);
    }

    @Transactional
    @Override
    public void updateProductQuantity(String id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",id,LocalDateTime.now()));
        product.setStockQuantity(quantity);
        productRepository.save(product);
    }


    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO,String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","Id",categoryId,LocalDateTime.now()));

        Product check = productRepository.findByName(productRequestDTO.getName());
        if(check!=null)
        {
            throw new ResourceAlreadyExistsException("Product","Product Name",productRequestDTO.getName(), LocalDateTime.now());
        }
        Product product = productMapper.toEntity(productRequestDTO);
        product.setCategory(category);
        product = productRepository.save(product);
        return productMapper.toDTO(product);
    }

    @Transactional
    @Override
    public ProductResponseDTO updateProduct(ProductRequestDTO productRequestDTO, String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",id,LocalDateTime.now()));

        Category category = categoryRepository.findByName(productRequestDTO.getCategoryName());
        if(category==null)
        {
            throw new ResourceNotFoundException("Category","Name",productRequestDTO.getCategoryName(),LocalDateTime.now());
        }

        product.setCategory(category);
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setImageUrl(productRequestDTO.getImageUrl());
        product.setActive(productRequestDTO.getActive());
        product.setPrice(productRequestDTO.getPrice());
        product.setStockQuantity(productRequestDTO.getStockQuantity());
        product.setSellerId(productRequestDTO.getSellerId());

        return productMapper.toDTO(productRepository.save(product));

    }

    @Transactional
    @Override
    public ProductResponseDTO deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product","ProductId",id,LocalDateTime.now()));

        productRepository.delete(product);
        return productMapper.toDTO(product);

    }
}
