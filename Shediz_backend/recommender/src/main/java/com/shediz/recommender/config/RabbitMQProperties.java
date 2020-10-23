package com.shediz.recommender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties
{
    private String queueName;
    private String exchangeName;
    private String userKey;
    private String scoreKey;
    private String postKey;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getUserKey()
    {
        return userKey;
    }

    public void setUserKey(String userKey)
    {
        this.userKey = userKey;
    }

    public String getScoreKey()
    {
        return scoreKey;
    }

    public void setScoreKey(String scoreKey)
    {
        this.scoreKey = scoreKey;
    }

    public String getPostKey()
    {
        return postKey;
    }

    public void setPostKey(String postKey)
    {
        this.postKey = postKey;
    }
}
