package com.study.realtimechat.repository;

import com.study.realtimechat.model.entity.FriendShipEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface FriendShipRepository extends ReactiveCrudRepository<FriendShipEntity, Long> {
    @Query("""
    select count(*) > 0
    from friendship
    where (friendship_A = :email1 and friendship_B = :email2) 
       or (friendship_A = :email2 and friendship_B = :email1)
    """)
    Mono<Boolean> existsBetween(String email1, String email2);

}
