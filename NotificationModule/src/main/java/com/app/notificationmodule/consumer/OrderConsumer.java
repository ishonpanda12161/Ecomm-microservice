package com.app.notificationmodule.consumer;

import com.app.notificationmodule.payload.OrderEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class OrderConsumer {

    @Bean
    public Consumer<OrderEvent> orderEventConsumer()
    {
        return orderEvent -> {
            String status = orderEvent.getStatus().toString();
            switch (status){
                case "CREATED" -> System.out.println("Created: ");
                case "CANCELLED" -> System.out.println("Cancelled: ");
                case "DELIVERED" -> System.out.println("Delivered: ");
                case "CONFIRMED" -> System.out.println("CONFIRMED: ");
                case "SHIPPED" -> System.out.println("SHIPPED: ");
            }
            System.out.println("Order -> \nID: "+ orderEvent.getId()+"\nSTATUS: "+orderEvent.getStatus()+"\nITEMS: "+orderEvent.getOrderItems().toString());
        };
    }
}
