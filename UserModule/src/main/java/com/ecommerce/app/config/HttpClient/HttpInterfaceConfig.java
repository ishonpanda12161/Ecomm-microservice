package com.ecommerce.app.config.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

//@Configuration
public class HttpInterfaceConfig {

//    @Value("${keycloak.uri}")
//    private String keycloakURI;
//
//    @Bean
//    public KeycloakServiceClient keycloakServiceClient()
//    {
//        RestClient restClient = RestClient
//                .builder()
//                .baseUrl(keycloakURI)
//                .build();
//
//        RestClientAdapter adapter = RestClientAdapter
//                .create(restClient);
//
//        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
//        return factory.createClient(KeycloakServiceClient.class);
//    }

}
