package com.ecommerce.app.config;

//@Configuration
public class RabbitMQConfiguration {

//    @Value("${rabbitmq.exchange.name}")
//    private String exchangeName;
//
//    @Bean
//    TopicExchange exchange()
//    {
//        return ExchangeBuilder
//                .topicExchange(exchangeName)
//                .durable(true)
//                .build();
//    }
//
//    @Bean
//    MessageConverter messageConverter()
//    {
//        return new JacksonJsonMessageConverter();
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory)
//    {
//        RabbitTemplate template = new RabbitTemplate(connectionFactory);
//        template.setMessageConverter(messageConverter());
//        template.setExchange(exchangeName);
//        return template;
//    }
}
