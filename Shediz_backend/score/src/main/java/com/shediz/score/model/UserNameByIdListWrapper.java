package com.shediz.score.model;

import java.util.List;

public class UserNameByIdListWrapper
{
    private String username;

    private List<String> ids;

    public UserNameByIdListWrapper()
    {
    }

    public UserNameByIdListWrapper(String username, List<String> ids)
    {
        this.username = username;
        this.ids = ids;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public List<String> getIds()
    {
        return ids;
    }

    public void setIds(List<String> ids)
    {
        this.ids = ids;
    }
}