package com.example.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

//@Component
@Configuration
public class ConsumerController {


    @Bean
    public Consumer<RiderLocation> processLocation()
    {
        return riderLocation -> System.out.println("[consumer:1]Recieved: ID: "+riderLocation.getId()+", GEO: "+riderLocation.getLat()+" "+riderLocation.getLon());
    }

    //    @KafkaListener(topics = "riderEvent",groupId = "group1")
//    public void listen1(RiderLocation riderLocation)
//    {
//        System.out.println("[consumer:1]Recieved: ID: "+riderLocation.getId()+", GEO: "+riderLocation.getLat()+" "+riderLocation.getLon());
//    }

}
