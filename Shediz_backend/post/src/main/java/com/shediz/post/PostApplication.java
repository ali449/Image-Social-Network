package com.shediz.post;

import com.shediz.spamdetect.SpamFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@SpringBootApplication
@EnableEurekaClient
@EnableAsync
public class PostApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(PostApplication.class, args);
    }

    @Bean(name = "threadPoolTaskExecutor")
    public Executor threadPoolTaskExecutor()
    {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public SpamFilter getSpamFilter()
    {
        return new SpamFilter();
    }
}
