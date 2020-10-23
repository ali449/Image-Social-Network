package com.shediz.post.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shediz.post.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPostConverter implements Converter<String, Post>
{

    private final ObjectMapper mapper;

    @Autowired
    public StringToPostConverter(ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override
    public Post convert(String s)
    {
        try
        {
            return mapper.readValue(s, Post.class);
        } catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
