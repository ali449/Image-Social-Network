package com.shediz.recommender.repository;

import com.shediz.recommender.domain.Post;
import org.neo4j.springframework.data.repository.ReactiveNeo4jRepository;
import org.neo4j.springframework.data.repository.query.Query;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


public interface PostRepository extends ReactiveNeo4jRepository<Post, String>
{
    @Transactional
    @Query("match (u:User {username: $username}) match (p:Post {pid: $pid}) create (u)-[:APPROVED]->(p)")
    Mono<Void> approve(String username, String pid);

    @Transactional
    @Query("match (u:User {username: $username})-[r:APPROVED]->(p:Post {pid: $pid}) delete r")
    Mono<Void> removeApprove(String username, String pid);

    @Transactional
    @Query("match (p:Post {pid: $pid}) set p.score = $score return p.score")
    Mono<Integer> updateScore(String pid, int score);
}
