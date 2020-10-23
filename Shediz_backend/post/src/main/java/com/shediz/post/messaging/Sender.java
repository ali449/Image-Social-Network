package com.shediz.post.messaging;

import com.shediz.post.config.RabbitMQProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class Sender
{
    private final RabbitTemplate rabbitTemplate;

    private final RabbitMQProperties properties;

    @Autowired
    public Sender(RabbitTemplate rabbitTemplate, RabbitMQProperties properties)
    {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Async("threadPoolTaskExecutor")
    public void sendMessage(GeneralMessage message)
    {
        System.out.println("Sending message: " + message);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRoutingKey(), message);
    }
}
