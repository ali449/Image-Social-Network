package com.shediz.score.messaging;

import com.shediz.score.service.MainService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class Receiver
{
    private final MainService service;

    public Receiver(MainService service)
    {
        this.service = service;
    }

    @RabbitListener(queues = {"${rabbitmq.queue2Name}", "${rabbitmq.queue3Name}"})
    public void onMessage(GeneralMessage message)
    {
        System.out.println("Received:" + message);

        switch (message.getEvent())
        {
            case POST_CREATED:
                service.addPost(message.getSourceUserName(), message.getPostId());
                break;
            case POST_DELETED:
                service.deletePost(message.getPostId());
                break;
            case USER_DELETED:
                service.deleteAllUserData(message.getSourceUserName());
                break;
            default:
                System.out.println("Can not parse message");
        }
    }
}
