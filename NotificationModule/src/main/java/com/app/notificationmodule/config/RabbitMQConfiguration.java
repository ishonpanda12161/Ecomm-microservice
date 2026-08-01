package com.app.notificationmodule.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.queue.name}")
    private String queueName;
    @Value("${rabbitmq.routing.order}")
    private String orderKey;
    @Bean
    Queue queue()
    {
        return QueueBuilder
                .durable(queueName)
                .build();
    }

    @Bean
    TopicExchange exchange()
    {
        return ExchangeBuilder
                .topicExchange(exchangeName)
                .durable(true)
                .build();
    }

    @Bean
    Binding orderBinding()
    {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(orderKey);
    }


    @Bean
    MessageConverter messageConverter()
    {
        return new JacksonJsonMessageConverter();
    }

//    @Value("${rabbitmq.queue.name}")
//    private String queueName;
//
//    @Bean
//    public Queue queue()
//    {
//        return QueueBuilder.durable(queueName)
//                .build();
//    }
//
//    @Bean
//    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory)
//    {
//        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
//        rabbitAdmin.setAutoStartup(true);
//        return rabbitAdmin;
//    }
//
//    @Bean
//    public MessageConverter messageConverter()
//    {
//        return new JacksonJsonMessageConverter();
//    }


}
