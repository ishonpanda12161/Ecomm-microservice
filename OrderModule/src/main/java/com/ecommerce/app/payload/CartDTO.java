package com.ecommerce.app.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartDTO {

    private String id;
    private BigDecimal totalPrice;
    private List<CartItemDTO> cartItemDTOs;

}
