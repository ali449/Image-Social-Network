package com.shediz.gateway.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.shediz.gateway.database.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class PreFilter extends ZuulFilter
{
    private static String GATEWAY_SECRET;

    @Value("${gateway-secret}")
    public void setGatewaySecret(String gatewaySecret)
    {
        GATEWAY_SECRET = gatewaySecret;
    }

    @Override
    public String filterType()
    {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder()
    {
        return 1;
    }

    @Override
    public boolean shouldFilter()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return auth != null && auth.getPrincipal() != null;
    }

    @Override
    public Object run()
    {
        RequestContext context = RequestContext.getCurrentContext();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        context.addZuulRequestHeader("Secret", GATEWAY_SECRET);
        context.addZuulRequestHeader("UserName", auth.getName());

        //Add is_private header if client wants to create post - POST http://localhost:8080/post
        String requestedPath = context.getRequest().getRequestURI();
        if (requestedPath.equals("/post") || requestedPath.equals("/post/"))
            context.addZuulRequestHeader("IsPrivate",
                    String.valueOf(((User) auth.getPrincipal()).getIsPrivate()));

        return null;
    }
}
