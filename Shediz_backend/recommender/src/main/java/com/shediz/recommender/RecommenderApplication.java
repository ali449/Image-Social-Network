package com.shediz.recommender;

import org.neo4j.springframework.data.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EnableReactiveNeo4jRepositories
public class RecommenderApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(RecommenderApplication.class, args);
    }

}
