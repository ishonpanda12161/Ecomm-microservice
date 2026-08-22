package com.ecommerce.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity)
    {
        return httpSecurity.authorizeHttpRequests(req -> req
                .requestMatchers(HttpMethod.GET, "/api/product/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/category/**").permitAll()
                .requestMatchers("/api/product/**").hasAnyRole("USER","SELLER","ADMIN")
                .requestMatchers("/api/category/**").hasAnyRole("USER","SELLER","ADMIN")
                .anyRequest().authenticated())
                .oauth2ResourceServer(auth -> auth.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(keycloakConverter())))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }

    @Bean
    public JwtAuthenticationConverter keycloakConverter()
    {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter( jwt  ->
        {
            Map<String,Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if(resourceAccess == null) return List.of();

            Map<String,Object> client = (Map<String, Object>) resourceAccess.get("oauth2-ecomm-pkce");
            if(client == null) return List.of();

            List<String> roles = (List<String>) client.get("roles");
            if(roles == null) return List.of();

            return roles.stream().map(role ->
                    new SimpleGrantedAuthority("ROLE_"+role))
                    .collect(Collectors.toList());
        });

        return converter;
    }
}
