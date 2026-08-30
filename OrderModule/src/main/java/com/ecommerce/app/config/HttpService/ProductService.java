package com.ecommerce.app.config.HttpService;

import com.ecommerce.app.config.HttpClient.ProductServiceClient;
import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.payload.ProductResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.naming.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductServiceClient productServiceClient;

    @Retry(name = "productRetry")
    @CircuitBreaker(name = "productBreaker")
    public ProductResponseDTO getProduct(String id) {
        return productServiceClient.getProduct(id);
    }

    @Retry(name = "productRetry")
    @CircuitBreaker(name = "productBreaker")
    public List<ProductResponseDTO> getBatch(Set<String> productIds)
    {
        return productServiceClient.getBatch(productIds);
    }

    @CircuitBreaker(name = "productBreaker",fallbackMethod = "stockUpdateFallback")
    public void decreaseProductQuantity(String id, Integer quantity) {
        productServiceClient.decreaseProductQuantity(id,quantity);
    }

    @CircuitBreaker(name = "productBreaker",fallbackMethod = "stockUpdateFallback")
    public void increaseProductQuantity(String id, Integer quantity) {
        productServiceClient.increaseProductQuantity(id,quantity);
    }

    public void stockUpdateFallback(String id,Integer quantity)
    {
        log.error("Stock Update in ProductModule failed for ID {}", id);
        throw new APIException(
                "Cannot udpate stock, Product service is unavailable",
                "PRODUCT service down",
                LocalDateTime.now()
        );
    }

    public void updateQuantityFallback(String id,Integer quantity,Throwable throwable)  {
        log.error("Update in ProductModule failed for ID {}: {}", id, throwable.getMessage());
        throw new APIException(
                "Cannot udpate product, Product service is unavailable",
                "PRODUCT service down",
                LocalDateTime.now()
        );
    }


}
