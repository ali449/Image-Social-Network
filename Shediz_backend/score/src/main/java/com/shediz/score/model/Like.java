package com.shediz.score.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table
@IdClass(LikeId.class)
public class Like
{
    @Id
    private String username;

    @Id
    @Column(name = "postid")
    private String postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postid", insertable = false, updatable = false)
    private Post post;

    public Like()
    {
    }

    public Like(String username, String postId)
    {
        this.username = username;
        this.postId = postId;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPostId()
    {
        return postId;
    }

    public void setPostId(String postId)
    {
        this.postId = postId;
    }

    @JsonIgnore
    public Post getPost()
    {
        return post;
    }

    public void setPost(Post post)
    {
        this.post = post;
    }

    @Override
    public String toString()
    {
        return username + " Liked " + postId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Like)) return false;
        Like like = (Like) o;
        return username != null && postId != null &&
                username.equals(like.getUsername()) &&
                postId.equals(like.getPostId());
    }

    @Override
    public int hashCode()
    {
        return 100;
    }
}
