package com.shediz.gateway.database;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;


@Entity
public class Request
{
    @EmbeddedId
    FollowId fid;

    public Request()
    {
        fid = new FollowId();
    }

    public Request(String fkFrom, String fkTo)
    {
        fid = new FollowId(fkFrom, fkTo);
    }

    public String getFkFrom()
    {
        return fid.getFkFrom();
    }

    public void setFkFrom(String fk_from)
    {
        fid.setFkFrom(fk_from);
    }

    public String getFkTo()
    {
        return fid.getFkTo();
    }

    public void setFkTo(String fk_to)
    {
        fid.setFkTo(fk_to);
    }
}
