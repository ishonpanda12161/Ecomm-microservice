package com.app.notificationmodule.consumer;

import com.app.notificationmodule.payload.OrderEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class OrderEventConsumer {

    @RabbitListener(queues="${rabbitmq.queue.name}")
    public void createdOrderEvent(OrderEvent orderEvent)
    {
        String status = orderEvent.getStatus().toString();
        switch (status){
            case "CREATED" -> System.out.println("Created: ");
            case "CANCELLED" -> System.out.println("Cancelled: ");
            case "DELIVERED" -> System.out.println("Delivered: ");
            case "CONFIRMED" -> System.out.println("CONFIRMED: ");
            case "SHIPPED" -> System.out.println("SHIPPED: ");
        }
        System.out.println("Order -> \nID: "+ orderEvent.getId()+"\nSTATUS: "+orderEvent.getStatus()+"\nITEMS: "+orderEvent.getOrderItems().toString());
    }
}
