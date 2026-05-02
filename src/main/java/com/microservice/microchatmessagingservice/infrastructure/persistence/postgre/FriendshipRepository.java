package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre;

import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.FriendshipEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {

    @Query("""
    SELECT f FROM FriendshipEntity f WHERE f.receiverId = :userId OR f.requesterId = :userId
    AND f.status = 'ACCEPTED'
""")
    List<FriendshipEntity> findAcceptedFriendIdsByUserId(Long userId);

    @Query("""
    SELECT COUNT(f) > 0 FROM FriendshipEntity f 
    WHERE (f.requesterId = :userA AND f.receiverId = :userB) 
       OR (f.requesterId = :userB AND f.receiverId = :userA)
""")
    boolean existsBetweenUsers(@Param("userA") Long userA, @Param("userB") Long userB);
}
