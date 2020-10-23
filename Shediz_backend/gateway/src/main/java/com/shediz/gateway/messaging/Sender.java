package com.shediz.gateway.messaging;

import com.shediz.gateway.config.RabbitMQProperties;
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
    public void sendMessageToQueue1(GeneralMessage message)
    {
        System.out.println("Sending message 1: " + message);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRouting1Key(), message);
    }

    //Delete account message
    @Async("threadPoolTaskExecutor")
    public void sendMessageToQueue2(GeneralMessage message)
    {
        System.out.println("Sending message 2: " + message);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRouting2Key(), message);
    }

    //Follow request message
    @Async("threadPoolTaskExecutor")
    public void sendMessageToQueue3(FollowMessage message)
    {
        System.out.println("Sending message 3: " + message);
        rabbitTemplate.convertAndSend(properties.getExchangeName(), properties.getRouting3Key(), message);
    }
}
