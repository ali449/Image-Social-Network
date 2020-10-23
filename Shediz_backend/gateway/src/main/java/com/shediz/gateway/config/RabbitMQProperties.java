package com.shediz.gateway.config;

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
    private String routing1Key;
    private String routing2Key;
    private String routing3Key;

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

    public String getRouting1Key()
    {
        return routing1Key;
    }

    public void setRouting1Key(String routing1Key)
    {
        this.routing1Key = routing1Key;
    }

    public String getRouting2Key()
    {
        return routing2Key;
    }

    public void setRouting2Key(String routing2Key)
    {
        this.routing2Key = routing2Key;
    }

    public String getRouting3Key()
    {
        return routing3Key;
    }

    public void setRouting3Key(String routing3Key)
    {
        this.routing3Key = routing3Key;
    }
}
