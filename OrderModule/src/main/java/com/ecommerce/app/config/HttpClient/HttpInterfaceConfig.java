package com.ecommerce.app.config.HttpClient;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpInterfaceConfig {

    @Autowired(required = false)
    private ObservationRegistry observationRegistry;
    @Autowired(required = false)
    private Tracer tracer;
    @Autowired(required = false)
    private Propagator propagator;

    @Bean
    @LoadBalanced
    public RestClient.Builder getRestClientBuilderLB()
    {
        RestClient.Builder builder = RestClient.builder();

        if(observationRegistry!=null)
        {
            builder.requestInterceptor(createTracingInterceptor());
        }
        return builder;
    }

    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return ((request, body, execution) ->{
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attributes!=null)
            {
                String auth = attributes.getRequest().getHeader("Authorization");
                if(auth!=null)
                {
                    request.getHeaders().add("Authorization",auth);
                }
            }
            if(tracer!=null && propagator!=null && tracer.currentSpan()!=null)
            {
                propagator.inject(tracer.currentTraceContext().context(),
                        request.getHeaders(),
                        ((carrier, key, value) -> {
                            carrier.add(key,value);
                        }));
            }
            return execution.execute(request,body);
        } );

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
