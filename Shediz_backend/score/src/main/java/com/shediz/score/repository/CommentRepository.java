package com.shediz.score.repository;

import com.shediz.score.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>
{
    List<Comment> findAllByPostId(String pid, Pageable pageable);

    Long countByPostIdAndIsNegativeFalse(String pid);

    @Transactional
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId = ?1")
    void deleteAllByPostId(String pid);
}
