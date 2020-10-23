package com.shediz.score.config;

import com.shediz.score.messaging.Receiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig
{
    private final RabbitMQProperties properties;

    @Autowired
    public RabbitMQConfig(RabbitMQProperties properties)
    {
        this.properties = properties;
    }

    @Bean
    public DirectExchange exchange()
    {
        return new DirectExchange(properties.getExchangeName(), false, false);
    }

    @Bean
    public Queue queue1()
    {
        return new Queue(properties.getQueue1Name(), false);
    }

    @Bean
    public Queue queue2()
    {
        return new Queue(properties.getQueue2Name(), false);
    }

    @Bean
    public Queue queue3()
    {
        return new Queue(properties.getQueue3Name(), false);
    }

    @Bean
    public Binding binding(Queue queue1, DirectExchange exchange)
    {
        return BindingBuilder.bind(queue1).to(exchange).with(properties.getRoutingKey());
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory)
    {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }
}
