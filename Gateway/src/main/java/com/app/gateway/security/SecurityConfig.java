package com.app.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

//@Configuration
//@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final KeycloakAuthRoleConverter keycloakAuthRoleConverter;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity httpSecurity)
    {
        return httpSecurity
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.POST,"/api/user").permitAll()
                        .pathMatchers("/api/product/**","/api/category/**").hasAnyRole("PRODUCT","USER","SELLER")
                        .pathMatchers("/api/user/**","/api/address/**").hasRole("USER")
                        .pathMatchers("/api/order/**","/api/cart/**").hasRole("USER")
                        .anyExchange().authenticated())
                .csrf(req -> req.disable())
                .oauth2ResourceServer(auth -> auth.jwt( jwt -> jwt.jwtAuthenticationConverter(keycloakAuthRoleConverter)))
                .build();
    }
}
