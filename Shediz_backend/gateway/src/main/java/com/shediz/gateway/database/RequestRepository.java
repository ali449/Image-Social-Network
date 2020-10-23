package com.shediz.gateway.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, FollowId>
{
}
