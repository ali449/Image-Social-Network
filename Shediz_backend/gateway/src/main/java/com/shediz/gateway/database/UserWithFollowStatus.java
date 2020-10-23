package com.shediz.gateway.database;


public class UserWithFollowStatus
{
    private final String username;

    private final boolean isEnabled;

    private final boolean isPrivate;

    private final String bio;

    private final Long numFollowing;

    private final Long numFollowers;

    private final boolean isFollowing;

    public UserWithFollowStatus(User user, boolean isFollowing)
    {
        this.username = user.getUsername();
        this.isEnabled = user.isEnabled();
        this.isPrivate = user.getIsPrivate();
        this.bio = user.getBio();
        this.numFollowing = user.getNumFollowing();
        this.numFollowers = user.getNumFollowers();
        this.isFollowing = isFollowing;
    }

    public String getUsername()
    {
        return username;
    }

    public boolean getIsEnabled()
    {
        return isEnabled;
    }

    public String getBio()
    {
        return bio;
    }

    public boolean getIsPrivate()
    {
        return isPrivate;
    }

    public Long getNumFollowing()
    {
        return numFollowing;
    }

    public Long getNumFollowers()
    {
        return numFollowers;
    }

    public boolean getIsFollowing()
    {
        return isFollowing;
    }
}

