package com.ecommerce.app.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDTO {


    @NotBlank(message = "Category Name cannot be blank.")
    @Size(min = 3,message = "Name must be at least 3 characters.")
    private String name;

}
