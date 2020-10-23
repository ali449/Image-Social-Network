package com.shediz.score.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/*
    Mapping according to below site:
    https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/
 */

@Entity
@Table
public class Post
{
    @Id
    private String pid;

    private String owner; //UserName of Post Owner

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "post")
    private final List<Like> likes = new ArrayList<>();

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "post")
    private final List<Comment> comments = new ArrayList<>();

    public void addLike(Like like)
    {
        likes.add(like);
        like.setPost(this);
    }

    /*public void removeLike(Like like)
    {
        likes.remove(like);
        like.setPost(null);
    }*/

    public void addComment(Comment comment)
    {
        comments.add(comment);
        comment.setPost(this);
    }

    /*public void removeComment(Comment comment)
    {
        comments.remove(comment);
        comment.setPost(null);
    }*/

    public Long getLastCommentId()
    {
        return comments.get(comments.size()-1).getCid();
    }

    public Comment findCommentById(Long cid)
    {
        return comments.stream().filter(c -> c.getCid().equals(cid)).findFirst().orElse(null);
    }

    public Post()
    {
    }

    public Post(String owner, String pid)
    {
        this.owner = owner;
        this.pid = pid;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }
}
