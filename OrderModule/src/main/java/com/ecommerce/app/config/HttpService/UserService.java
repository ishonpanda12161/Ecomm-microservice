package com.ecommerce.app.config.HttpService;

import com.ecommerce.app.config.HttpClient.UserServiceClient;
import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.payload.UserResponseDTO;
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
public class UserService{

    private final UserServiceClient userServiceClient;

    @CircuitBreaker(name = "userBreaker",fallbackMethod = "getUserFallback")
    public UserResponseDTO getUser(String id) {
        return userServiceClient.getUser(id);
    }

    public UserResponseDTO getUserFallback(String id,Throwable throwable) {
        log.error("UserModule call failed for ID {}: {}", id, throwable.getMessage());
        throw new APIException(
                "Cannot get User, User service is unavailable",
                "USER service down",
                LocalDateTime.now()
        );
    }
}
