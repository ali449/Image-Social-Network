package com.shediz.recommender.domain;


public class SearchedTagWrapper
{
    private String tag;

    private Integer count;

    public SearchedTagWrapper(String tag, Integer count)
    {
        this.tag = tag;
        this.count = count;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public Integer getCount()
    {
        return count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }
}
