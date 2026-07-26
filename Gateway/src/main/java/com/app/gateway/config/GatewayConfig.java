package com.app.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder)
    {
        return builder.routes()
                .route("USERMODULE",r ->
                    r.path("/api/user/**","/api/address/**")
//                            .filters(f -> f.rewritePath("/user(?<segment>/?.*)","/api/user${segment}")
//                                    .rewritePath("/address(?<segment>/?.*)","/api/address${segment}"))
                            .uri("lb://USERMODULE")
                )
                .route("PRODUCTMODULE",r ->
                        r.path("/api/product/**","/api/category/**")
                                .uri("lb://PRODUCTMODULE")
                )
                .route("ORDERMODULE",r ->
                        r.path("/api/cart/**","/api/order/**")
                                .uri("lb://ORDERMODULE")
                )
                .route("Eureka-server",r ->
                        r.path("/eureka/main")
                                .filters(f -> f.rewritePath("/eureka/main","/"))
                                .uri("http://localhost:8761"))
                .route("Eureka-static",r ->
                        r.path("/eureka/**")
                                .uri("http://localhost:8761"))
                .build();
    }
}
