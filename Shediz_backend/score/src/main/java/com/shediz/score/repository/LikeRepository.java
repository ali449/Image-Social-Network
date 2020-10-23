package com.shediz.score.repository;

import com.shediz.score.model.Like;
import com.shediz.score.model.LikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId>
{
    Long countByPostId(String pid);

    @Modifying
    @Query("DELETE FROM Like l WHERE l.postId = ?1")
    void deleteAllByPostId(String pid);
}
