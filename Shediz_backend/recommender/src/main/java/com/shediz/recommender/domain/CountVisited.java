package com.shediz.recommender.domain;

import org.neo4j.springframework.data.core.schema.RelationshipProperties;


/*
    Count Visited Posts in Specific Tag.
    Attribute Relationship between User and Tag.
 */

@RelationshipProperties
public class CountVisited
{
    private final int count;

    public CountVisited(int count)
    {
        this.count = count;
    }

    public int getCount()
    {
        return count;
    }
}
