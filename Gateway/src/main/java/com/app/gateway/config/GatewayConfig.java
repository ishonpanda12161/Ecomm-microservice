package com.app.gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Base64;

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

    //set key. rate limit by user id
    @Bean
    public KeyResolver jwtKeyResolver()
    {
        return exchange ->
        {
            String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if(auth==null || !auth.startsWith("Bearer "))
            {
                return Mono.just("anonymous");
            }

            String token = auth.substring(7);
            try{
                String[] parts = token.split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(payload);
                JsonNode sub = json.get("sub");

                return sub != null
                        ? Mono.just(sub.asText())
                        : Mono.just("anonymous");
            }
            catch (Exception e)
            {
                return Mono.just("anonymous");
            }
        };
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
                                            config.setRateLimiter(redisRateLimiter()).setKeyResolver(jwtKeyResolver())))
                            .uri("lb://USERMODULE")
                )
                .route("PRODUCTMODULE",r ->
                        r.path("/api/product/**","/api/category/**")
                                .filters(f -> f
                                        .circuitBreaker(config -> config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/PRODUCT"))
                                        .requestRateLimiter(config ->
                                                config.setRateLimiter(redisRateLimiter()).setKeyResolver(jwtKeyResolver())))
                                .uri("lb://PRODUCTMODULE")
                )
                .route("ORDERMODULE",r ->
                        r.path("/api/cart/**","/api/order/**")
                                .filters(f -> f
                                        .circuitBreaker(config -> config.setName("gatewayBreaker").setFallbackUri("forward:/fallback/ORDER"))
                                        .requestRateLimiter(config ->
                                                config.setRateLimiter(redisRateLimiter()).setKeyResolver(jwtKeyResolver()))
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
