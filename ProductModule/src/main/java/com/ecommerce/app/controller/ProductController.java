package com.ecommerce.app.controller;


import com.ecommerce.app.config.AppConfig;
import com.ecommerce.app.payload.ProductRequestDTO;
import com.ecommerce.app.payload.ProductResponseDTO;
import com.ecommerce.app.payload.ProductSearchResponseDTO;
import com.ecommerce.app.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping()
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts()
    {
        return ResponseEntity.ok().body(productService.getAllProducts());
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductResponseDTO>> getBatchProducts(
            @RequestBody Set<String> productIds
    )
    {
        return ResponseEntity.ok().body(productService.getBatchProducts(productIds));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable String id
    )
    {
        return ResponseEntity.ok().body(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ProductSearchResponseDTO> searchProduct(
            @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
            @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_PRODUCTS_BY,required = false) String sortBy,
            @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    )
    {
        return ResponseEntity.ok().body(productService.searchProducts(pageNum,pageSize,sortBy,sortDir));
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<ProductSearchResponseDTO> searchProductByKeyword(
        @PathVariable String keyword,
        @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
        @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
        @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_PRODUCTS_BY,required = false) String sortBy,
        @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    )
    {
        return ResponseEntity.ok().body(productService.searchProductsByKeyword(keyword,pageNum,pageSize,sortBy,sortDir));
    }

    @GetMapping("/search/category/{category}")
    public ResponseEntity<ProductSearchResponseDTO> searchProductByCategory(
        @PathVariable String category,
        @RequestParam(name = "pageNum",defaultValue = AppConfig.PAGE_NUMBER,required = false) Integer pageNum,
        @RequestParam(name = "pageSize",defaultValue = AppConfig.PAGE_SIZE,required = false) Integer pageSize,
        @RequestParam(name = "sortBy",defaultValue = AppConfig.SORT_PRODUCTS_BY,required = false) String sortBy,
        @RequestParam(name = "sortDir",defaultValue = AppConfig.SORT_DIR,required = false) String sortDir
    )
    {
        return ResponseEntity.ok().body(productService.searchProductsByCategory(category,pageNum,pageSize,sortBy,sortDir));
    }

    @PreAuthorize("hasRole('SELLER')")
    @PostMapping("/{categoryId}")
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestBody @Valid ProductRequestDTO productRequestDTO,
            @PathVariable String categoryId
            )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(productRequestDTO,categoryId));
    }


    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @RequestBody @Valid ProductRequestDTO productRequestDTO,
            @PathVariable String id
    )
    {
        return ResponseEntity.ok().body(productService.updateProduct(productRequestDTO,id));
    }

    @PutMapping("/dec/{id}/{quantity}")
    public ResponseEntity<Void> decreaseProductQuantity(
            @PathVariable Integer quantity,
            @PathVariable String id
    )
    {
        productService.decreaseProductQuantity(id,quantity);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/inc/{id}/{quantity}")
    public ResponseEntity<Void> increaseProductQuantity(
            @PathVariable Integer quantity,
            @PathVariable String id
    )
    {
        productService.increaseProductQuantity(id,quantity);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id
    )
    {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
