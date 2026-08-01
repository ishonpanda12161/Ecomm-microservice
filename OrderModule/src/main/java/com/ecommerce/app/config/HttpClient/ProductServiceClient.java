package com.ecommerce.app.config.HttpClient;

import com.ecommerce.app.payload.ProductResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange
public interface ProductServiceClient {


    @GetExchange("/api/product/{id}")
    ProductResponseDTO getProduct(@PathVariable String id);

    @PutExchange("/api/product/{id}/{quantity}")
    void updateProductQuantity(
            @PathVariable String id,
            @PathVariable Integer quantity
    );

}
