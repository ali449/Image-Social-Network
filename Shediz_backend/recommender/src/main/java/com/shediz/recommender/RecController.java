package com.shediz.recommender;

import com.shediz.recommender.domain.SearchedTagWrapper;
import com.shediz.recommender.service.RecService;
import static com.shediz.recommender.utils.Util.*;

import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
public class RecController
{
    private final RecService service;

    private static final int PAGE_SIZE = 10;

    @Autowired
    public RecController(RecService service)
    {
        this.service = service;
    }

    //Call by client
    @GetMapping
    ResponseEntity<org.neo4j.driver.internal.shaded.reactor.core.publisher.Mono<?>>
    recommend(@RequestParam int page, HttpServletRequest request)
    {
        String userName = request.getHeader("UserName");

        if (isNotFromGateWay(request) || isEmpty(userName))
            return ResponseEntity.badRequest().body(org.neo4j.driver.internal.shaded.reactor.core.publisher.Mono
                    .just("Invalid parameters"));

        int skip = page * PAGE_SIZE;

        return ResponseEntity.ok(service.suggest(userName, skip, PAGE_SIZE).collectList());
    }

    //Call by client
    @PutMapping(value = "/searched_tag", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<String>> incrementSearchedTags(@RequestBody SearchedTagWrapper stw,
                                                       HttpServletRequest request)
    {
        String userName = request.getHeader("UserName");

        if (isEmpty(stw.getTag()) || isNotFromGateWay(request) || isEmpty(userName))
            return Mono.just(ResponseEntity.badRequest().body("Invalid parameters"));

        return service.incrementSearchedTags(userName, stw.getTag(), stw.getCount())
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Update function called"));
    }

    //Call by client
    @DeleteMapping("/rm_searched_tag")
    Mono<ResponseEntity<String>> removeSearchedTag(@RequestParam String tag,
                                                    HttpServletRequest request)
    {
        String userName = request.getHeader("UserName");

        if (isEmpty(tag) || isNotFromGateWay(request) || isEmpty(userName))
            return Mono.just(ResponseEntity.badRequest().body("Invalid parameters"));

        return service.removeSearchedTag(userName, tag)
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Delete function called"));
    }

    //Call by client
    @DeleteMapping("/rm_searched_tag_all")
    Mono<ResponseEntity<String>> removeSearchedTags(HttpServletRequest request)
    {
        String userName = request.getHeader("UserName");

        if (isNotFromGateWay(request) || isEmpty(userName))
            return Mono.just(ResponseEntity.badRequest().body("Invalid parameters"));

        return service.removeSearchedTags(userName)
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Delete function called"));
    }

    /*//Call by user service
    @PutMapping("/add_user_rp")
    Mono<ResponseEntity<String>> increaseUserRP(@RequestParam String username)
    {
        return service.increaseUserRP(username).flatMap(result -> Mono
                .just(ResponseEntity.ok("Updated to " + result)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND).body("Nothing found!")));
    }

    //Call by user service
    @PutMapping("/reduce_user_rp")
    Mono<ResponseEntity<String>> decreaseUserRP(@RequestParam String username)
    {
        return service.decreaseUserRP(username).flatMap(result -> Mono
                .just(ResponseEntity.ok("Updated to " + result)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND).body("Nothing found!")));
    }

    //Call by score service
    @PutMapping("/update_score")
    Mono<ResponseEntity<String>> updatePostScore(@RequestParam String pid, @RequestParam int score)
    {
        return service.updatePostScore(pid, score).flatMap(result -> Mono
                .just(ResponseEntity.status(HttpStatus.ACCEPTED).body("Updated to " + result)));
    }

    //Call by score service
    @PutMapping("/approve")
    Mono<ResponseEntity<String>> approve(@RequestParam String username, @RequestParam String pid)
    {
        return service.approvePost(username, pid)
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Create function called"));
    }

    //Call by score service
    @DeleteMapping("/rm_approve")
    Mono<ResponseEntity<String>> removeApprove(@RequestParam String username, @RequestParam String pid)
    {
        return service.removeApprovePost(username, pid)
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Delete function called"));
    }

    //Call by post service
    @PostMapping(value = "/add_post", consumes = MediaType.APPLICATION_JSON_VALUE)
    Mono<ResponseEntity<String>> createNewPost(@RequestBody(required = false) PostWrapper post)
    {
        if (post.getPid().isEmpty() || post.getUsername().isEmpty())
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empty parameters"));

        return service.savePost(post.getUsername(), post.getPid(), post.getTags())
                .map(str -> ResponseEntity.status(HttpStatus.CREATED).body(str))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND).body("Returned post id is empty")));
    }

    //Call by user service
    @PostMapping("/add_user")
    Mono<ResponseEntity<String>> createNewUser(@RequestParam String username)
    {
        if (username.isEmpty())
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UserName is empty"));

        return service.saveUser(username)
                .map(str -> ResponseEntity.status(HttpStatus.CREATED).body(str))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND).body("Returned username is empty")));
    }

    //Call by post service
    @DeleteMapping("/rm_post")
    Mono<ResponseEntity<String>> deletePost(@RequestParam String pid)
    {
        if (pid.isEmpty())
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Post id is empty"));

        return service.deletePost(pid)
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Delete function called"));
    }

    //Call by user service
    @DeleteMapping("/rm_user")
    Mono<ResponseEntity<String>> deleteUser(@RequestParam String username)
    {
        if (username.isEmpty())
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("UserName is empty"));

        return service.deleteUser(username)
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("Delete function called"));
    }*/
}
