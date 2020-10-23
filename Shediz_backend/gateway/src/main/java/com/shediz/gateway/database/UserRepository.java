package com.shediz.gateway.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>
{
    List<User> findTop10ByUsernameContainingIgnoreCase(String username);

    Optional<User> findOneByUsername(String username);

    Optional<User> findByToken(String token);

    List<User> findByUsernameIn(List<String> username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.token = ?1 WHERE u.username = ?2")
    void setToken(String token, String username);
}
