package com.shediz.score.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public final class GeneralMessage implements Serializable
{
    /*
        All event types in system:
        user_created, user_deleted, user_followed, user_unfollowed
        post_created, post_deleted, score_approved, score_unapproved
    */

    public enum EventType
    {
        USER_DELETED, POST_CREATED, POST_DELETED,
        SCORE_APPROVED, SCORE_UNAPPROVED
    }

    private final EventType event;

    private final String sourceUserName;

    private final String targetUserName;

    private final String postId;

    private final List<String> tags;

    public GeneralMessage(@JsonProperty("event") EventType event,
                          @JsonProperty("source_username") String sourceUserName,
                          @JsonProperty("target_username") String targetUserName,
                          @JsonProperty("pid") String postId,
                          @JsonProperty("tags") List<String> tags)
    {
        this.event = event;
        this.sourceUserName = sourceUserName;
        this.targetUserName = targetUserName;
        this.postId = postId;
        this.tags = tags;
    }

    public EventType getEvent()
    {
        return event;
    }

    public String getSourceUserName()
    {
        return sourceUserName;
    }

    public String getTargetUserName()
    {
        return targetUserName;
    }

    public String getPostId()
    {
        return postId;
    }

    public List<String> getTags()
    {
        return tags;
    }

    @Override
    public String toString()
    {
        return "GeneralMessage{" +
                "event=" + event +
                ", sourceUserName='" + sourceUserName + '\'' +
                ", targetUserName='" + targetUserName + '\'' +
                ", postId='" + postId + '\'' +
                ", tags=" + tags +
                '}';
    }
}
