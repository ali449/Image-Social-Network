package com.shediz.recommender.domain;

import org.neo4j.springframework.data.core.schema.Id;
import org.neo4j.springframework.data.core.schema.Node;
import org.neo4j.springframework.data.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("Post")
public class Post
{
    @Id
    private final String pid;

    private final int score;

    @Relationship(type = "OWNER", direction = Relationship.Direction.OUTGOING)
    private User owner;

    @Relationship(type = "APPROVED", direction = Relationship.Direction.INCOMING)
    private List<User> approvers = new ArrayList<>();

    @Relationship(type = "TAGS", direction = Relationship.Direction.OUTGOING)
    private List<Tag> tags = new ArrayList<>();

    public Post(String pid, int score)
    {
        this.pid = pid;
        this.score = score;
    }

    public void setOwner(User owner)
    {
        this.owner = owner;
    }

    public String getPid()
    {
        return pid;
    }

    public int getScore()
    {
        return score;
    }

    public User getOwner()
    {
        return owner;
    }

    public List<User> getApprovers()
    {
        return approvers;
    }

    public List<Tag> getTags()
    {
        return tags;
    }
}
