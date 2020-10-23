package com.shediz.post.model;

import org.springframework.web.multipart.MultipartFile;

public class PostWrapper
{
    private String content;

    private MultipartFile file;

    public PostWrapper()
    {
    }

    public PostWrapper(String content, MultipartFile file)
    {
        this.content = content;
        this.file = file;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public MultipartFile getFile()
    {
        return file;
    }

    public void setFile(MultipartFile file)
    {
        this.file = file;
    }
}
