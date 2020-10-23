package com.shediz.score.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@Table
public class Comment
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cid;

    private String username;

    @Column(name = "postid")
    private String postId;

    private String text;

    @Column(name = "is_negative")
    private boolean isNegative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postid", insertable = false, updatable = false)
    private Post post;

    public Comment()
    {
    }

    public Long getCid()
    {
        return cid;
    }

    public void setCid(Long cid)
    {
        this.cid = cid;
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

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @JsonIgnore
    public boolean getIsNegative()
    {
        return isNegative;
    }

    public void setIsNegative(boolean negative)
    {
        isNegative = negative;
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
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return cid != null && cid.equals(comment.getCid());
    }

    @Override
    public int hashCode()
    {
        return 100;
    }
}
