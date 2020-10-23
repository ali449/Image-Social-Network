package com.shediz.recommender.messaging;

import com.shediz.recommender.service.RecService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Receiver
{
    private final RecService service;

    @Autowired
    public Receiver(RecService service)
    {
        this.service = service;
    }

    @RabbitListener(queues = {"${rabbitmq.queueName}"})
    public void onMessage(GeneralMessage message)
    {
        System.out.println(message);
        switch (message.getEvent())
        {
            case USER_CREATED:
                service.saveUser(message.getSourceUserName()).subscribe();
                break;
            case USER_DELETED:
                service.deleteUser(message.getSourceUserName()).subscribe();
                break;
            case USER_FOLLOWED:
                service.increaseUserRP(message.getTargetUserName()).subscribe();
                break;
            case USER_UNFOLLOWED:
                service.decreaseUserRP(message.getTargetUserName()).subscribe();
                break;
            case POST_CREATED:
                service.savePost(message.getSourceUserName(), message.getPostId(), message.getTags()).subscribe();
                break;
            case POST_DELETED:
                service.deletePost(message.getPostId()).subscribe();
                break;
            case SCORE_APPROVED:
                service.approvePost(message.getSourceUserName(), message.getPostId()).subscribe();
                break;
            case SCORE_UNAPPROVED:
                service.removeApprovePost(message.getSourceUserName(), message.getPostId()).subscribe();
                break;
        }
    }
}
