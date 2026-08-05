package com.example.producer;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Random;
import java.util.function.Supplier;

@Configuration
public class ProducerController {

    @Bean
    public Supplier<Message<RiderLocation>> sendLocation()
    {
        Random random = new Random();
        return () -> {
            int id = random.nextInt(50);
            RiderLocation riderLocation = new RiderLocation(String.valueOf(id),"12312.123123","9879.23","34i+23j-123k");
            return MessageBuilder.withPayload(riderLocation).setHeader(KafkaHeaders.KEY,String.valueOf(id).getBytes()).build();
        };
    }

    @Bean
    public Supplier<Message<String>> statusUpdate()
    {
        Random random = new Random();
        return () -> {
            int id = random.nextInt(10);
            boolean status = random.nextBoolean();
            String res = "Status: "+status+" Rider ID: "+id;
            return MessageBuilder.withPayload(res).setHeader(KafkaHeaders.KEY,String.valueOf(id).getBytes()).build();
        };
    }
}
