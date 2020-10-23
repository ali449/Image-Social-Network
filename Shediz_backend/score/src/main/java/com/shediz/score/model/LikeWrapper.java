package com.shediz.score.model;

public class LikeWrapper
{
    private final String postId;

    private final Long numLikes;

    private final boolean isUserLiked;

    public LikeWrapper(String postId, Long numLikes, boolean isUserLiked)
    {
        this.postId = postId;
        this.numLikes = numLikes;
        this.isUserLiked = isUserLiked;
    }

    public String getPostId()
    {
        return postId;
    }

    public Long getNumLikes()
    {
        return numLikes;
    }

    public boolean getIsUserLiked()
    {
        return isUserLiked;
    }
}
