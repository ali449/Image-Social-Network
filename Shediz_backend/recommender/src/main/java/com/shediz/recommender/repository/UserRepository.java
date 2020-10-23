package com.shediz.recommender.repository;

import com.shediz.recommender.domain.User;
import org.neo4j.springframework.data.repository.ReactiveNeo4jRepository;
import org.neo4j.springframework.data.repository.query.Query;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveNeo4jRepository<User, String>
{
    @Transactional
    @Query("match (:User {username: $username})<-[o:OWNER]-(p:Post) match (p)-[r]-(:User) delete r " +
            "with p optional match (p)-[ptr:TAGS]-(t:Tag) delete ptr " +
            "with t, p optional match (t) where size((t)-[:TAGS]-())=0 detach delete t " +
            "with p match (p) delete p")
    Mono<Void> removeUserPosts(String username);

    @Query("match (:User {username: $username})-[r:SEARCHED]-(t:Tag {name: $tag}) delete r " +
            "with t match (t) where size((t)--()) = 0 delete t")
    Mono<Void> removeSearchedTag(String username, String tag);

    @Query("match (:User {username: $username})-[r:SEARCHED]-(t:Tag) delete r " +
            "with t match (t) where size((t)--()) = 0 delete t")
    Mono<Void> removeSearchedTags(String username);

    @Query("match (u:User {username: $username}) merge (t:Tag {name: $tag}) " +
            "merge(u)-[r:SEARCHED]-(t) on create set r.count=1 on match set r.count=r.count+$countToAdd")
    Mono<Void> increaseSearchedTag(String username, String tag, int countToAdd);
}
