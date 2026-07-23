package com.ecommerce.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class HttpInterfaceConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder getRestClientBuilderLB()
    {
        return RestClient.builder();
    }

    @Bean
    @Primary
    public RestClient.Builder getRestClientBuilder()
    {
        return RestClient.builder();
    }

    @Bean
    @Primary
    public RestClient getRestClient(RestClient.Builder builder)
    {
        return builder.build();
    }

    private RestClient getRestClientLB(RestClient.Builder builder, String SERVICE_NAME)
    {
        return builder.baseUrl("http://"+SERVICE_NAME)
                .build();
    }

    @Bean
    public ProductServiceClient productServiceClient(@Qualifier("getRestClientBuilderLB") RestClient.Builder builder)
    {
        RestClient restClient = getRestClientLB(builder,"ProductModule");
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(ProductServiceClient.class);
    }

    @Bean
    public UserServiceClient userServiceClient(@Qualifier("getRestClientBuilderLB") RestClient.Builder builder)
    {
        RestClient restClient = getRestClientLB(builder,"UserModule");
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(UserServiceClient.class);
    }

}
