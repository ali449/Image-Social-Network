package com.shediz.post.model;

import java.util.List;

public class IdsWrapper
{
    private List<String> ids;

    public IdsWrapper()
    {
    }

    public IdsWrapper(List<String> ids)
    {
        this.ids = ids;
    }

    public List<String> getIds()
    {
        return ids;
    }

    public void setIds(List<String> ids)
    {
        this.ids = ids;
    }
}