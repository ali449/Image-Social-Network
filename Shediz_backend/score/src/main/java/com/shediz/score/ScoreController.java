package com.shediz.score;

import com.shediz.score.messaging.GeneralMessage;
import com.shediz.score.messaging.Sender;
import com.shediz.score.model.*;
import com.shediz.score.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.shediz.score.utils.Util.*;

@RestController
public class ScoreController
{
    private static final int COMMENT_PAGE_SIZE = 10;

    private final MainService service;

    private final Sender publisher;

    @Autowired
    public ScoreController(MainService service, Sender sender)
    {
        this.service = service;
        this.publisher = sender;
    }

    @PostMapping("/add_like")
    ResponseEntity<?> addLike(@RequestParam String pid, HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Only GateWay can send this request");

        if (isBlank(pid))
            return ResponseEntity.badRequest().build();


        String sourceUserName = request.getHeader("UserName");
        if (isBlank(sourceUserName))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UserName not found in header");


        String ownerUserName = service.getPostOwner(pid);
        if (isBlank(ownerUserName))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't fetch post owner");

        if (checkAccessGetBlocking(sourceUserName, ownerUserName))
        {
            if (!service.addLike(sourceUserName, pid))
                return ResponseEntity.notFound().build();

            //Notify to recommender service
            publisher.sendMessage(new GeneralMessage(GeneralMessage.EventType.SCORE_APPROVED,
                    sourceUserName, null, pid, null));

            return ResponseEntity.ok(Collections.singletonMap("success", true));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("User " + sourceUserName + " must follow " + ownerUserName);
    }

    @DeleteMapping("/rm_like")
    ResponseEntity<?> unlike(@RequestParam String pid, HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Only GateWay can send this request");

        if (isBlank(pid))
            return ResponseEntity.badRequest().build();


        String sourceUserName = request.getHeader("UserName");
        if (isBlank(sourceUserName))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UserName not found in header");


        Optional<Like> optionalLike = service.getLike(pid, sourceUserName);
        if (optionalLike.isPresent())
        {
            Like like = optionalLike.get();

            if (like.getUsername().equals(sourceUserName))
            {
                service.deleteLike(like);

                //Notify to recommender service
                publisher.sendMessage(new GeneralMessage(GeneralMessage.EventType.SCORE_UNAPPROVED,
                        sourceUserName, null, pid, null));

                return ResponseEntity.ok(Collections.singletonMap("success", true));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User " + sourceUserName + " can't do this");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find username liker for " + pid);
    }

    @PostMapping(value = "/add_comment", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> addComment(@RequestParam String pid, @RequestBody CommentWrapper comment,
                                 HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();

        if (isBlank(pid) || isBlank(comment.getText()))
            return ResponseEntity.badRequest().build();


        String sourceUserName = request.getHeader("UserName");
        if (isBlank(sourceUserName))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UserName not found in header");


        String ownerUserName = service.getPostOwner(pid);
        if (isBlank(ownerUserName))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't fetch post owner");

        if (checkAccessGetBlocking(sourceUserName, ownerUserName))
        {
            boolean isNegativeComment = isNegativeText(comment.getText());

            Long result = service.addComment(sourceUserName, pid, comment.getText(), isNegativeComment);
            if (result == null)
                return ResponseEntity.notFound().build();


            //Notify to recommender service
            if (!isNegativeComment)
                publisher.sendMessage(new GeneralMessage(GeneralMessage.EventType.SCORE_APPROVED,
                        sourceUserName, null, pid, null));

            return ResponseEntity.ok(Collections.singletonMap("cid", result));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("User " + sourceUserName + " must follow " + ownerUserName);
    }

    @DeleteMapping("/rm_comment")
    ResponseEntity<?> removeComment(@RequestParam Long cid, HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Only GateWay can send this request");

        if (cid == null)
            return ResponseEntity.badRequest().build();


        String sourceUserName = request.getHeader("UserName");
        if (isBlank(sourceUserName))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UserName not found in header");


        Comment comment = service.getComment(cid);
        if (comment != null)
        {
            if (comment.getUsername().equals(sourceUserName))
            {
                service.deleteComment(comment);

                //Notify to recommender service
                publisher.sendMessage(new GeneralMessage(GeneralMessage.EventType.SCORE_UNAPPROVED,
                        sourceUserName, null, comment.getPostId(), null));

                return ResponseEntity.ok(Collections.singletonMap("success", true));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User " + sourceUserName + " can't do this");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Can't find username for " + cid);
    }

    @GetMapping("/s/score")
    ResponseEntity<Long> getPostScore(@RequestParam String pid)
    {
        if (isBlank(pid))
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(service.countPostScore(pid));
    }


    @GetMapping("/s/likes_pid_list")
    ResponseEntity<List<LikeWrapper>> getPostLikesByPidList_1(@RequestBody UserNameByIdListWrapper ids)
    {
        if (ids.getIds() == null || ids.getIds().isEmpty() || ids.getIds().size() > COMMENT_PAGE_SIZE ||
            isBlank(ids.getUsername()))
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(service.getLikes(ids.getIds(), ids.getUsername()));
    }

    //PutMapping for some clients doesn't support request body on get method
    @PutMapping("/s/likes_pid_list")
    ResponseEntity<List<LikeWrapper>> getPostLikesByPidList_2(@RequestBody UserNameByIdListWrapper ids)
    {
        return getPostLikesByPidList_1(ids);
    }

    @GetMapping("/s/likes")
    ResponseEntity<Long> countPostLikes(@RequestParam String pid)
    {
        if (isBlank(pid))
            return ResponseEntity.badRequest().build();

        return ResponseEntity.ok(service.countPostLike(pid));
    }

    @GetMapping("/comments")
    ResponseEntity<?> getComments(@RequestParam String pid, @RequestParam int page, HttpServletRequest request)
    {
        if (isNotFromGateWay(request))
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Only GateWay can send this request");

        if (isBlank(pid))
            return ResponseEntity.badRequest().build();


        String sourceUserName = request.getHeader("UserName");
        if (isBlank(sourceUserName))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UserName not found in header");


        String ownerUserName = service.getPostOwner(pid);

        if (isBlank(ownerUserName))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (checkAccessGetBlocking(sourceUserName, ownerUserName))
            return ResponseEntity.ok(service.getComments(pid, page, COMMENT_PAGE_SIZE));

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("User " + sourceUserName + " must follow " + ownerUserName);
    }
}
