package com.ecommerce.app.payload;

import com.ecommerce.app.model.OrderItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderItemSearchResponseDTO {

    public List<OrderItem> orderItems = new ArrayList<>();
    private Integer pageNum;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;

}
