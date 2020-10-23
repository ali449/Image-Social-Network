package com.shediz.gateway.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
    class for hold follow request messages to private users
 */
public class FollowMessage
{
    private final String sourceUserName;

    private final String targetUserName;

    public FollowMessage(@JsonProperty String sourceUserName, @JsonProperty String targetUserName)
    {
        this.sourceUserName = sourceUserName;
        this.targetUserName = targetUserName;
    }

    public String getSourceUserName()
    {
        return sourceUserName;
    }

    public String getTargetUserName()
    {
        return targetUserName;
    }

    @Override
    public String toString()
    {
        return "FollowMessage{" +
                "sourceUserName='" + sourceUserName + '\'' +
                ", targetUserName='" + targetUserName + '\'' +
                '}';
    }
}
