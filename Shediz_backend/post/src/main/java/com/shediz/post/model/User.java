package com.shediz.post.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User
{
    private String username;

    private boolean isPrivate;

    public User()
    {
    }

    public User(String username, boolean isPrivate)
    {
        this.username = username;
        this.isPrivate = isPrivate;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public boolean getIsPrivate()
    {
        return isPrivate;
    }

    public void setIsPrivate(boolean aPrivate)
    {
        isPrivate = aPrivate;
    }

    @Override
    public String toString()
    {
        return "User{" + "username='" + username + '\'' + ", isPrivate=" + isPrivate + '}';
    }
}
