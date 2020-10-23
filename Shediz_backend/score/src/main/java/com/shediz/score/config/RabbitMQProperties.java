package com.shediz.score.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitMQProperties
{
    private String queue1Name;
    private String queue2Name;
    private String queue3Name;
    private String exchangeName;
    private String routingKey;

    public String getQueue1Name()
    {
        return queue1Name;
    }

    public void setQueue1Name(String queue1Name)
    {
        this.queue1Name = queue1Name;
    }

    public String getQueue2Name()
    {
        return queue2Name;
    }

    public void setQueue2Name(String queue2Name)
    {
        this.queue2Name = queue2Name;
    }

    public String getQueue3Name()
    {
        return queue3Name;
    }

    public void setQueue3Name(String queue3Name)
    {
        this.queue3Name = queue3Name;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}
