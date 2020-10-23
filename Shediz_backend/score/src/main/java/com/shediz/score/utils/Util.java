package com.shediz.score.utils;

import com.shediz.score.service.SentimentAnalysis;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@Service
public class Util
{
    private static String MAIN_URL;

    private static String GATEWAY_SECRET;

    @Value("${gateway-secret}")
    public void setGatewaySecret(String gatewaySecret)
    {
        GATEWAY_SECRET = gatewaySecret;
    }

    @Value("http://${auth-host}:8080")
    public void setMainUrl(String mainUrl)
    {
        MAIN_URL = mainUrl;
    }

    //This is a blocking http client that is an anti pattern for microservices.
    //We most convert all function to non blocking, but sqlite doesn't support reactive.
    public static Boolean checkAccessGetBlocking(String fromUserName, String toUserName)
    {
        final String url = MAIN_URL + "/s/can_get/" + fromUserName + "/" + toUserName;

        return new RestTemplate().getForObject(url, Boolean.class);
    }

    public static boolean isNegativeText(String text)
    {
        boolean isNegative = false;
        try
        {
            String polarity = SentimentAnalysis.predictOnce(SentimentAnalysis.getModel(), text);

            if (polarity.equals("neg"))
                isNegative = true;

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return isNegative;
    }

    public static boolean isNotFromGateWay(HttpServletRequest request)
    {
        String secret = request.getHeader("Secret");

        return isBlank(secret) || !secret.equals(GATEWAY_SECRET);
    }

    public static boolean isBlank(String str)
    {
        return str == null || str.isEmpty();
    }
}
