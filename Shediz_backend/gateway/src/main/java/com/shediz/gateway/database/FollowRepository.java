package com.shediz.gateway.database;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, FollowId>
{
    List<Follow> findByFid_FkFrom(String userName, Pageable pageable);

    List<Follow> findByFid_FkTo(String userName, Pageable pageable);

    Long countByFid_FkFrom(String userName);

    Long countByFid_FkTo(String uid);

    Optional<Follow> findByFid_FkFromAndFid_FkTo(String fromUserName, String toUserName);

    @Modifying
    @Transactional
    @Query("DELETE FROM Follow f WHERE f.fid.fkFrom=?1 OR f.fid.fkTo=?1")
    void deleteAllFollow(String userName);
}
