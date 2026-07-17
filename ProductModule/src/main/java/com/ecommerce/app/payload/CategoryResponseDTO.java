package com.ecommerce.app.payload;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponseDTO {

    private String id;
    private String name;
    private LocalDateTime createdAt;
}
