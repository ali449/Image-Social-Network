package com.shediz.recommender.domain;

import java.util.List;

public class PostWrapper
{
    private String username;

    private String pid;

    private List<String> tags;

    public PostWrapper()
    {
    }

    public PostWrapper(String username, String pid, List<String> tags)
    {
        this.username = username;
        this.pid = pid;
        this.tags = tags;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }
}
