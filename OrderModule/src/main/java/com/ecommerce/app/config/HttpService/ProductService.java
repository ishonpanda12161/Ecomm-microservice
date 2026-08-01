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

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductServiceClient productServiceClient;


//    @Retry(name = "productRetry",fallbackMethod = "getProductFallback")
//    @CircuitBreaker(name = "productBreaker")
    @CircuitBreaker(name = "productBreaker")
    @Retry(name = "productRetry")
    public ProductResponseDTO getProduct(String id) {
        return productServiceClient.getProduct(id);
    }

    @Retry(name = "productRetry",fallbackMethod = "updateQuantityFallback")
    @CircuitBreaker(name = "productBreaker")
    public void updateProductQuantity(String id, Integer quantity) {
        productServiceClient.updateProductQuantity(id,quantity);
    }

    public void updateQuantityFallback(String id,Integer quantity,Throwable throwable)  {
        log.error("Update in ProductModule failed for ID {}: {}", id, throwable.getMessage());
        throw new APIException(
                "Cannot udpate product, Product service is unavailable",
                "PRODUCT service down",
                LocalDateTime.now()
        );
    }

//    public ProductResponseDTO getProductFallback(String id,Throwable throwable){
//        log.error("ProductModule call failed for ID {}: {}", id, throwable.getMessage());
//        throw new APIException(
//                "Cannot get product, Product service is unavailable",
//                "PRODUCT service down",
//                LocalDateTime.now()
//        );
//    }
}
