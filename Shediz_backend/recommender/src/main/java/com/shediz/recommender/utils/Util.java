package com.shediz.recommender.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class Util
{
    private static String GATEWAY_SECRET;

    @Value("${gateway-secret}")
    public void setGatewaySecret(String gatewaySecret)
    {
        GATEWAY_SECRET = gatewaySecret;
    }

    public static boolean isNotFromGateWay(HttpServletRequest request)
    {
        String secret = request.getHeader("Secret");

        return isEmpty(secret) || !secret.equals(GATEWAY_SECRET);
    }

    public static boolean isEmpty(String str)
    {
        return str == null || str.isEmpty();
    }
}
