package com.shediz.gateway.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Entity
public class User implements UserDetails
{
    @Id
    private String username;

    private String password;

    private String token;

    @Column(name = "is_enabled")
    private boolean isEnabled;

    @Column(name = "is_private")
    private boolean isPrivate;

    private String bio;

    @Transient
    private Long numFollowing;

    @Transient
    private Long numFollowers;

    public User()
    {

    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @JsonIgnore
    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public boolean getIsEnabled()
    {
        return isEnabled;
    }

    public void setIsEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public void setIsPrivate(boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    public boolean getIsPrivate()
    {
        return isPrivate;
    }

    public Long getNumFollowing()
    {
        return numFollowing;
    }

    public void setNumFollowing(Long numFollowing)
    {
        this.numFollowing = numFollowing;
    }

    public Long getNumFollowers()
    {
        return numFollowers;
    }

    public void setNumFollowers(Long numFollowers)
    {
        this.numFollowers = numFollowers;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return new ArrayList<>();
    }

    @JsonIgnore
    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled()
    {
        return isEnabled;
    }
}
