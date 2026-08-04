package com.example.producer;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.converter.KafkaMessageHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.Message;
import java.util.Random;
import java.util.function.Supplier;

//@RestController
//@RequestMapping("/producer")
//public class ProducerController {
//
//    private final KafkaTemplate<String,RiderLocation> kafkaTemplate;
//
//    public ProducerController(KafkaTemplate<String, RiderLocation> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    @PostMapping("/send")
//    public String send()
//    {
//        kafkaTemplate.send("riderEvent",new RiderLocation("111","12312.123123","9879.23","34i+23j-123k"));
//        return "sent";
//    }
//}


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
