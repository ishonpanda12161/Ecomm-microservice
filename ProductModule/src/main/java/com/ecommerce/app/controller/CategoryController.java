package com.ecommerce.app.controller;

import com.ecommerce.app.payload.CategoryRequestDTO;
import com.ecommerce.app.payload.CategoryResponseDTO;
import com.ecommerce.app.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping()
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @RequestBody @Valid CategoryRequestDTO categoryRequestDTO
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryRequestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategory(
            @PathVariable String id
    )
    {
        return ResponseEntity.ok().body(categoryService.getCategory(id));
    }

    @GetMapping()
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategory(
    )
    {
        return ResponseEntity.ok().body(categoryService.getAllCategory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable String id,
            @RequestBody @Valid CategoryRequestDTO categoryRequestDTO
    )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.updateCategory(id,categoryRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable String id
    )
    {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
