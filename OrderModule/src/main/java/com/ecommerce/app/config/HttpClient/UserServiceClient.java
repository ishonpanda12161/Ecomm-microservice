package com.ecommerce.app.config.HttpClient;

import com.ecommerce.app.payload.UserResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserServiceClient {

    @GetExchange("/api/user/keycloak/{keycloakId}")
    UserResponseDTO getUser(@PathVariable String keycloakId);

}
