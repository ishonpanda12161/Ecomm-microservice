package com.ecommerce.app.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "Category Name cannot be blank.")
    @Size(min = 3,message = "Name must be at least 3 characters.")
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "category",cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    private List<Product> products = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

}
