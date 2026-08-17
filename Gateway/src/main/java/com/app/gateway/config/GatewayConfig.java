package com.app.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class GatewayConfig {

    @Value("${gateway.ratelimit.replenish-rate}")
    private int replenishRate;
    @Value("${gateway.ratelimit.burst-capacity}")
    private int burstCapacity;

    @Bean
    public RedisRateLimiter redisRateLimiter()
    {
        return new RedisRateLimiter(replenishRate,burstCapacity);
    }

    @Bean
    public KeyResolver hostNameKeyResolver()
    {
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getHostName()
        );
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder)
    {
        return builder.routes()
                .route("USERMODULE",r ->
                    r.path("/api/user/**","/api/address/**")
                            .filters(f -> f
                                    .circuitBreaker(config -> config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/USER"))
                                    .requestRateLimiter(config ->
                                            config.setRateLimiter(redisRateLimiter()).setKeyResolver(hostNameKeyResolver())))
                            .uri("lb://USERMODULE")
                )
                .route("PRODUCTMODULE",r ->
                        r.path("/api/product/**","/api/category/**")
                                .filters(f -> f
                                        .circuitBreaker(config -> config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/PRODUCT"))
                                        .requestRateLimiter(config ->
                                                config.setRateLimiter(redisRateLimiter()).setKeyResolver(hostNameKeyResolver())))
                                .uri("lb://PRODUCTMODULE")
                )
                .route("ORDERMODULE",r ->
                        r.path("/api/cart/**","/api/order/**")
                                .filters(f -> f
                                        .circuitBreaker(config -> config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/ORDER"))
                                        .requestRateLimiter(config ->
                                                config.setRateLimiter(redisRateLimiter()).setKeyResolver(hostNameKeyResolver()))
                                )
                                .uri("lb://ORDERMODULE")
                )
                .route("Eureka-server",r ->
                        r.path("/eureka/main")
                                .filters(f -> f
                                        .rewritePath("/eureka/main","/").circuitBreaker(config -> config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/EUREKA")))

                                .uri("lb://EUREKASERVER"))
                .route("Eureka-static",r ->
                        r.path("/eureka/**")
                                .uri("lb://EUREKASERVER"))
                .build();
    }
}
