package com.example.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.function.Consumer;

@Configuration
public class ConsumerController {


    @Bean
    public Consumer<RiderLocation> processLocation()
    {
        return riderLocation -> System.out.println("[consumer:1]Recieved: ID: "+riderLocation.getId()+", GEO: "+riderLocation.getLat()+" "+riderLocation.getLon());
    }

    @Bean
    public Consumer<String> processStatus()
    {
        return status -> System.out.println("Recieved Location: "+status);
    }

}
