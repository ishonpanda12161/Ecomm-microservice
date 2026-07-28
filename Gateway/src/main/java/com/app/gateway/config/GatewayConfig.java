package com.app.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter()
    {
        return new RedisRateLimiter(1,1,1);
    }

    @Bean
    public KeyResolver hostNameKeyResolver()
    {
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getHostName()
        );
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder)
    {
        return builder.routes()
                .route("USERMODULE",r ->
                    r.path("/api/user/**","/api/address/**")
                            .filters(f -> f.circuitBreaker(config ->
                                    config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/USER")))
                            .uri("lb://USERMODULE")
                )
                .route("PRODUCTMODULE",r ->
                        r.path("/api/product/**","/api/category/**")
                                .filters(f -> f.circuitBreaker(config ->
                                        config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/PRODUCT")))
                                .uri("lb://PRODUCTMODULE")
                )
                .route("ORDERMODULE",r ->
                        r.path("/api/cart/**","/api/order/**")
                                .filters(f -> f.circuitBreaker(config ->
                                        config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/ORDER"))
                                        .requestRateLimiter(config ->
                                                config.setRateLimiter(redisRateLimiter()).setKeyResolver(hostNameKeyResolver()))
                                )
                                .uri("lb://ORDERMODULE")
                )
                .route("Eureka-server",r ->
                        r.path("/eureka/main")
                                .filters(f -> f.rewritePath("/eureka/main","/").circuitBreaker(config ->
                                        config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/EUREKA")))

                                .uri("http://localhost:8761"))
                .route("Eureka-static",r ->
                        r.path("/eureka/**")
                                .uri("http://localhost:8761"))
                .build();
    }
}
