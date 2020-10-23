package com.shediz.recommender.service;

import com.shediz.recommender.domain.CountVisited;
import com.shediz.recommender.domain.Post;
import com.shediz.recommender.domain.Tag;
import com.shediz.recommender.domain.User;
import com.shediz.recommender.repository.PostRepository;
import com.shediz.recommender.repository.UserRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.internal.shaded.reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.neo4j.driver.reactive.RxSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecService
{
    private static final int LEVEL1_USER_POST = 3;

    private static final int LEVEL2_POST_USER = 10;

    private static final int MAX_TAG = 3;

    private final Driver driver;

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    @Autowired
    public RecService(UserRepository userRepository, PostRepository postRepository, Driver driver)
    {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.driver = driver;
    }

    public Flux<String> suggest(String userName, int skip, int limit)
    {
        //TODO sort limited items by timestamp, now those are sorted by id(node)
        String query = "match (u:User {username: $username})-[:APPROVED]->(p:Post) with p, u limit $l1up " +
                "match (p)--(ua:User) with u, ua order by ua.reputation limit $l2pu " +
                "match (ua)-[:APPROVED]->(pr:Post) with pr, u " +
                "match (u)-[s:SEARCHED]->(t:Tag) with s, t, pr, u order by s.count limit $max_tag " +
                "match (t)<-[:TAGS]-(pt:Post) with collect(pt) + collect(pr) as rec, u " +
                "unwind rec as result match(result) where not (u)--(result) " +
                "with distinct result order by result.score desc " +
                "return result.pid skip $skip limit $limit";

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("username", userName);
        parameters.put("l1up", LEVEL1_USER_POST);
        parameters.put("l2pu", LEVEL2_POST_USER);
        parameters.put("max_tag", MAX_TAG);
        parameters.put("skip", skip);
        parameters.put("limit", limit);

        return Flux.usingWhen(org.neo4j.driver.internal.shaded.reactor.core.publisher.Mono
                .fromSupplier(driver::rxSession), session ->
                session.readTransaction(tx -> Flux.from(tx.run(query, parameters).records())
                        .map(record -> record.get(0).asString())
        ), RxSession::close);
    }

    public Mono<Integer> updatePostScore(String pid, int newScore)
    {
        return postRepository.updateScore(pid, newScore);
    }

    public Mono<Void> incrementSearchedTags(String userName, String tag, int countVisited)
    {
        return userRepository.increaseSearchedTag(userName, tag, countVisited);
    }

    public Mono<Void> removeSearchedTag(String userName, String tag)
    {
        return userRepository.removeSearchedTag(userName, tag);
    }

    public Mono<Void> removeSearchedTags(String userName)
    {
        return userRepository.removeSearchedTags(userName);
    }

    public Mono<Void> approvePost(String userName, String pid)
    {
        return postRepository.approve(userName, pid);
    }

    public Mono<Void> removeApprovePost(String userName, String pid)
    {
        return postRepository.removeApprove(userName, pid);
    }

    public Mono<String> savePost(String userName, String pid, List<String> tags)
    {
        return userRepository.findById(userName).flatMap(result -> {
                Post newPost = new Post(pid, 0);
                newPost.setOwner(result);

                if (tags != null && !tags.isEmpty())
                    newPost.getTags().addAll(tags.stream().map(Tag::new).collect(Collectors.toList()));

                Mono<Post> postSave = postRepository.save(newPost);
                return postSave.map(Post::getPid);
        });
    }

    public Mono<Integer> increaseUserRP(String userName)
    {
        return userRepository.findById(userName).flatMap(user -> {
           user.setReputation(user.getReputation() + 1);
           return userRepository.save(user).map(User::getReputation);
        });
    }

    public Mono<Integer> decreaseUserRP(String userName)
    {
        return userRepository.findById(userName).flatMap(user -> {
            user.setReputation(user.getReputation() - 1);
            return userRepository.save(user).map(User::getReputation);
        });
    }

    public Mono<String> saveUser(String userName)
    {
        return userRepository.save(new User(userName, 0)).map(User::getUsername);
    }

    public Mono<Void> deletePost(String pid)
    {
        return postRepository.deleteById(pid);
    }

    public Mono<Void> deleteUser(String userName)
    {
        return userRepository.removeUserPosts(userName)
                .doOnSuccess(x -> userRepository.deleteById(userName).subscribe());
    }

    @Transactional
    public reactor.core.publisher.Flux<String> insert()
    {
        User u0 = new User("u0", 1);
        User u1 = new User("u1", 1);
        User u2 = new User("u2", 1);
        User u3 = new User("u3", 1);
        User u4 = new User("u4", 1);
        User u5 = new User("u5", 1);


        Post p1 = new Post("p1", 1);
        Post p2 = new Post("p2", 2);
        Post p3 = new Post("p3", 3);
        Post p4 = new Post("p4", 4);
        Post p5 = new Post("p5", 5);
        Post p6 = new Post("p6", 6);
        Post p7 = new Post("p7", 7);
        Post p8 = new Post("p8", 8);
        Post p9 = new Post("p9", 8);
        Post p10 = new Post("p10", 10);


        p2.getTags().add(new Tag("nice"));
        p9.getTags().add(new Tag("hi"));
        p10.getTags().addAll(Arrays.asList(new Tag("hi"), new Tag("bye")));

        u1.getSearchedTag().put(new Tag("nice"), new CountVisited(10));
        u1.getSearchedTag().put(new Tag("hi"), new CountVisited(3));


        p1.setOwner(u1);
        p2.setOwner(u2);
        p3.setOwner(u3);
        p4.setOwner(u4);
        p5.setOwner(u5);
        p6.setOwner(u1);
        p7.setOwner(u2);
        p8.setOwner(u3);
        p9.setOwner(u4);
        p10.setOwner(u5);


        u0.getApproved().add(p4);

        u1.getApproved().add(p1);
        u1.getApproved().add(p2);
        u1.getApproved().add(p3);
        u1.getApproved().add(p4);
        u1.getApproved().add(p5);

        u2.getApproved().add(p3);
        u2.getApproved().add(p4);
        u2.getApproved().add(p9);

        u3.getApproved().add(p7);

        u4.getApproved().add(p1);

        u5.getApproved().add(p5);
        u5.getApproved().add(p6);
        u5.getApproved().add(p7);
        u5.getApproved().add(p8);

        reactor.core.publisher.Flux<String> postFlux = postRepository
                .saveAll(Flux.just(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10)).map(Post::getPid);
        reactor.core.publisher.Flux<String> userFlux = userRepository
                .saveAll(Flux.just(u0, u1, u2, u3, u4, u5)).map(User::getUsername);


        return reactor.core.publisher.Flux.concat(userFlux, postFlux);
    }
}
