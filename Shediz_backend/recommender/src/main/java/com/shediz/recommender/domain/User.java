package com.shediz.recommender.domain;

import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.core.schema.Relationship;

import java.util.*;

@Node("User")
public class User
{
    @Id
    private String username;

    private int reputation;

    @Relationship(type = "APPROVED", direction = Relationship.Direction.OUTGOING)
    private List<Post> approved = new ArrayList<>();

    @Relationship(type = "SEARCHED", direction = Relationship.Direction.OUTGOING)
    private Map<Tag, CountVisited> searchedTag = new HashMap<>();

    public User()
    {

    }

    public User(String username, int reputation)
    {
        this.username = username;
        this.reputation = reputation;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setReputation(int reputation)
    {
        this.reputation = reputation;
    }

    public String getUsername()
    {
        return username;
    }

    public int getReputation()
    {
        return reputation;
    }

    public List<Post> getApproved()
    {
        return approved;
    }

    public Map<Tag, CountVisited> getSearchedTag()
    {
        return searchedTag;
    }
}
