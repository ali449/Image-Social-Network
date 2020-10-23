package com.shediz.recommender.domain;

import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;


@Node("Tag")
public class Tag
{
    @Id
    private final String name;

    public Tag(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
