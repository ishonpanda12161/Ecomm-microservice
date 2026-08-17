package com.ecommerce.app.repository;

import com.ecommerce.app.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,String> {
    Product findByName(@NotBlank(message = "Product Name cannot be blank.") @Size(min = 3,message = "Name must be at least 3 characters.") String name);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);

    Page<Product> findByCategory_Name(String category, Pageable pageable);

    @Modifying
    @Query(
            """
            update Product  p
            set p.stockQuantity = p.stockQuantity - :quantity
            where p.id = :productId
            and p.stockQuantity >= :quantity
            """
    )
    int decreaseStock(String productId,int quantity);

    @Modifying
    @Query(
            """
            update Product  p
            set p.stockQuantity = p.stockQuantity + :quantity
            where p.id = :productId
            """
    )
    int increaseStock(String productId, Integer quantity);
}

