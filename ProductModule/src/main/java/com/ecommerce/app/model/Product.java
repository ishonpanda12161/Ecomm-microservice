package com.ecommerce.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("active = true")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String sellerId;

    @NotBlank(message = "Product Name cannot be blank.")
    @Size(min = 3,message = "Name must be at least 3 characters.")
    private String name;

    private String description;

    @NotNull(message = "Product Price cannot be null.")
    @Positive
    private BigDecimal price;

    private BigDecimal discount = BigDecimal.ZERO;

    @Min(value = 0,message = "Stock cannot be less than 0.")
    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String imageUrl;

    private Boolean active = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
