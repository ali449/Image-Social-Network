package com.shediz.post.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.*;

public class Post
{
    private String id;

    private String username;

    private boolean isPrivate;

    private String content;

    @JsonSerialize(using = ToStringSerializer.class)
    private MyDate date;

    private List<String> tags;

    private boolean isSpam;

    public Post()
    {
    }

    public static Post buildFromSource(String id, Map<String, Object> map)
    {
        Post post = new Post();

        post.setId(id);

        post.setUsername((String) map.get("username"));

        post.setIsPrivate((boolean) map.get("is_private"));

        post.setDate(new MyDate((String) map.get("date")));

        post.setContent((String) map.get("content"));

        post.setTags(getTags(map.get("tags")));

        post.setIsSpam((boolean) map.get("is_spam"));

        return post;
    }

    private static List<String> getTags(Object var)
    {
        List<String> result = new ArrayList<>();
        if (var instanceof List)
        {
            for(int i = 0; i < ((List<?>)var).size(); i++)
            {
                Object item = ((List<?>) var).get(i);
                if(item instanceof String)
                {
                    result.add((String) item);
                }
            }
        }
        return result;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    @JsonIgnore
    public boolean getIsPrivate()
    {
        return isPrivate;
    }

    public void setIsPrivate(boolean aPrivate)
    {
        isPrivate = aPrivate;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public MyDate getDate()
    {
        return date;
    }

    public void setDate(MyDate date)
    {
        this.date = date;
    }

    @JsonIgnore
    public List<String> getTags()
    {
        return tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }

    public boolean getIsSpam()
    {
        return isSpam;
    }

    public void setIsSpam(boolean isSpam)
    {
        this.isSpam = isSpam;
    }

    @Override
    public String toString()
    {
        return "Post{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", isPrivate=" + isPrivate +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", tags=" + tags +
                ", isSpam=" + isSpam +
                '}';
    }
}
