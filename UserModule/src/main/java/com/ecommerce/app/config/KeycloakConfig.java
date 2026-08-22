package com.ecommerce.app.config;

import lombok.NoArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
public class KeycloakConfig {

    @Value("${keycloak.uri}")
    private String keycloakURI;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.manage.clientId}")
    private String clientId;
    @Value("${keycloak.manage.secret}")
    private String secret;


    @Bean
    public Keycloak getInstance()
    {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakURI)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(secret)
                .build();

        return keycloak;
    }
}
