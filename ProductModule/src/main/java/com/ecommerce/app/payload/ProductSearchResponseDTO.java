package com.ecommerce.app.payload;

import lombok.Data;

import java.util.List;

@Data
public class ProductSearchResponseDTO {

    private List<ProductResponseDTO> productResponseDTOS;
    private Integer pageNum;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
