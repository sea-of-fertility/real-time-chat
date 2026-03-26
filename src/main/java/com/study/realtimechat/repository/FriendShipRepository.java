package com.study.realtimechat.repository;

import com.study.realtimechat.model.entity.FriendShipEntity;
import com.study.realtimechat.user.domain.response.FriendListResponse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FriendShipRepository extends ReactiveCrudRepository<FriendShipEntity, Long> {
    @Query("""
    select count(*) > 0
    from friendship
    where (friendship_A = :email1 and friendship_B = :email2) 
       or (friendship_A = :email2 and friendship_B = :email1)
    """)
    Mono<Boolean> existsBetween(String email1, String email2);

    @Query("""
    select u.email, u.nickname
    from friendship f 
    join users u on u.email = case
        when f.friendship_A = :email then f.friendship_B
        else f.friendship_A
    end
    where f.friendship_A = :email or f.friendship_B = :email
    """)
    Flux<FriendListResponse> findFriendList(String email);
}