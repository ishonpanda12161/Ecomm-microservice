package com.example.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic riderEvent()
    {
        return new NewTopic("riderEvent",3,(short)1);
    }

    @Bean
    public NewTopic riderStatus()
    {
        return new NewTopic("riderStatus",2,(short)1);
    }
}
