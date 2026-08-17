package com.ecommerce.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cart_items")
@RequiredArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @NotBlank(message = "Product Id cannot be blank.")
    private String productId;

    @Positive
    @NotNull(message = "Quantity Cannot be null.")
    private Integer quantity;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;


    public CartItem(@NotBlank(message = "Product Id cannot be blank.") String productId, @Positive @NotNull(message = "Quantity Cannot be null.") Integer quantity) {
        this.productId = productId;
        this.quantity =  quantity;
    }
}
