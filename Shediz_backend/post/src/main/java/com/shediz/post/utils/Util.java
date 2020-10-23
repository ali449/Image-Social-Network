package com.shediz.post.utils;

import com.shediz.post.model.Post;
import com.shediz.post.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Util
{
    private static String GATEWAY_SECRET;

    @Value("http://${auth-host}:8080")
    private String MAIN_URL;

    private static WebClient webClient;

    @Bean
    public WebClient webClient()
    {
        return WebClient.create(MAIN_URL);
    }

    @Autowired
    public void setWebClient(WebClient w)
    {
        webClient = w;
    }

    @Value("${gateway-secret}")
    public void setGatewaySecret(String gatewaySecret)
    {
        GATEWAY_SECRET = gatewaySecret;
    }

    //Fetch Requested Users Info From GateWay
    public Flux<User> getMultiUserInfo(List<String> userNames)
    {
        String str = userNames.toString();
        String strList = str.substring(1, str.length()-1);//Remove Braces
        final String url = "/s/multi_user?userNames=" + strList;//Create comma separated list

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(User.class);
    }

    //Fetch Requested User Info From GateWay
    public Mono<User> getUserInfoNonBlocking(String username)
    {
        final String url = "/s/user/" + username + "?justBaseInfo=true";

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(User.class);
    }

    //Fetch Requested User Info From GateWay
    public User getUserInfoBlocking(String username)
    {
        final String url = MAIN_URL + "/s/user/" + username + "?justBaseInfo=true";

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(url, User.class);
    }

    public Mono<String[]> getUserFollowingNamesNonBlocking(String username)
    {
        final String url = "/s/following/" + username;

        return webClient
                .get()
                .uri(url)
                .header("Secret", GATEWAY_SECRET)
                .retrieve()
                .bodyToMono(String[].class);
    }

    public String[] getUserFollowingNamesBlocking(String username)
    {
        final String url = "/s/following/" + username;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Secret", GATEWAY_SECRET);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String[].class);

        return responseEntity.getBody();
    }

    public Mono<Boolean> checkAccessGetUser(String fromUserName, String toUserName)
    {
        if (fromUserName != null && fromUserName.equals(toUserName))
            return Mono.just(true);

        final String url = "/s/can_get/" + fromUserName + "/" + toUserName;

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public Mono<Boolean> checkAccessGetPost(String fromUserName, Post post)
    {
        if (!post.getIsPrivate() || (fromUserName != null && fromUserName.equals(post.getUsername())))
            return Mono.just(true);

        final String url = "/s/can_get/" + fromUserName + "/" + post.getUsername();

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    public static List<String> fetchHashTags(String content)
    {
        List<String> hashTags = new ArrayList<>();

        Pattern pattern = Pattern.compile("#(\\S+)");
        Matcher matcher = pattern.matcher(content.toLowerCase());

        while (matcher.find())
            hashTags.add(matcher.group(1));

        return hashTags;
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
