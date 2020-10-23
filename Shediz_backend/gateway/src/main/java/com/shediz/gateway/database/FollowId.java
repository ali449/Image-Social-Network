package com.shediz.gateway.database;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FollowId implements Serializable
{
    private String fkFrom;

    private String fkTo;

    public FollowId()
    {

    }

    public FollowId(String fkFrom, String fkTo)
    {
        this.fkFrom = fkFrom;
        this.fkTo = fkTo;
    }

    public String getFkFrom()
    {
        return fkFrom;
    }

    public void setFkFrom(String fkFrom)
    {
        this.fkFrom = fkFrom;
    }

    public String getFkTo()
    {
        return fkTo;
    }

    public void setFkTo(String fkTo)
    {
        this.fkTo = fkTo;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof FollowId))
            return false;

        FollowId that = (FollowId) obj;
        return Objects.equals(fkFrom, that.fkFrom) &&
                Objects.equals(fkTo, that.fkTo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(fkFrom, fkTo);
    }
}