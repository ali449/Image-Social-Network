package com.shediz.post.messaging;

import com.shediz.post.service.PostService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class Receiver
{
    private final PostService postService;

    public Receiver(PostService postService)
    {
        this.postService = postService;
    }

    @RabbitListener(queues = {"${rabbitmq.queue3Name}"})
    public void onMessage(GeneralMessage message)
    {
        System.out.println("Received:" + message);

        if (message.getEvent() == GeneralMessage.EventType.USER_DELETED)
            postService.deleteByUserName(message.getSourceUserName()).subscribe();
        else
            System.out.println("Can not parse message");
    }
}
