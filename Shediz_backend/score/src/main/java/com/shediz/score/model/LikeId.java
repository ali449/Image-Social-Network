package com.shediz.score.model;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

public class LikeId implements Serializable
{
    private String username;

    @Column(name = "postid")
    private String postId;

    public LikeId()
    {
    }

    public LikeId(String username, String postId)
    {
        this.username = username;
        this.postId = postId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof LikeId)) return false;
        LikeId likeId = (LikeId) o;
        return Objects.equals(username, likeId.username) &&
                Objects.equals(postId, likeId.postId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(username, postId);
    }
}
