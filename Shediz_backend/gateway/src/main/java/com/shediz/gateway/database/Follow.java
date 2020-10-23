package com.shediz.gateway.database;

import javax.persistence.*;

@Entity
public class Follow
{
    @EmbeddedId
    FollowId fid;

    public Follow()
    {
        fid = new FollowId();
    }

    public Follow(String fkFrom, String fkTo)
    {
        fid = new FollowId(fkFrom, fkTo);
    }

    public Follow(FollowId fid)
    {
        this.fid = fid;
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
