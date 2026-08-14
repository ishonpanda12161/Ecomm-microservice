package com.app.gateway.security;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class KeycloakAuthRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ReactiveJwtAuthenticationConverter converter;
    @Value("${keycloak.clientId}")
    private String clientid;
    public KeycloakAuthRoleConverter()
    {

        converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt ->
        {
            Map<String,Object> resource = jwt.getClaimAsMap("resource_access");
            Map<String,Object> client = (Map<String, Object>) resource.get(clientid);
            List<String> roles = (List<String>) client.get("roles");

            return Flux.fromIterable(roles).map(role->new SimpleGrantedAuthority("ROLE_"+role));
        });

    }
    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        return converter.convert(jwt);
    }
}
