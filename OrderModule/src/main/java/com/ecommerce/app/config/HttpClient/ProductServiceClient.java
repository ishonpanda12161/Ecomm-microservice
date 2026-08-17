package com.ecommerce.app.config.HttpClient;

import com.ecommerce.app.payload.ProductResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.Set;

@HttpExchange
public interface ProductServiceClient {


    @GetExchange("/api/product/{id}")
    ProductResponseDTO getProduct(@PathVariable String id);

    @PostExchange("/api/product/batch")
    List<ProductResponseDTO> getBatch(@RequestBody Set<String> productIds);

    @PutExchange("/api/product/dec/{id}/{quantity}")
    void decreaseProductQuantity(
            @PathVariable String id,
            @PathVariable Integer quantity
    );

    @PutExchange("/api/product/inc/{id}/{quantity}")
    void increaseProductQuantity(
            @PathVariable String id,
            @PathVariable Integer quantity
    );

}
