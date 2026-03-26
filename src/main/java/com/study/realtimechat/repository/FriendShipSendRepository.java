package com.study.realtimechat.repository;

import com.study.realtimechat.model.entity.FriendRequestEntity;
import com.study.realtimechat.model.enums.FriendRequestStatus;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface FriendShipSendRepository extends ReactiveCrudRepository<FriendRequestEntity, Long> {
    Mono<Boolean>existsByFromEmailAndToEmailAndStatus(String from, String to, FriendRequestStatus status);
    Flux<FriendRequestEntity> findByToEmailAndStatus(String to, FriendRequestStatus status);
    Flux<FriendRequestEntity>findByFromEmailAndStatus(String from, FriendRequestStatus status);

    @Query("""
    SELECT fr.id AS requestId, fr.from_email AS fromEmail, u.nickname AS fromNickname, fr.created_at AS createdAt
    FROM friend_request fr
    JOIN users u ON fr.from_email = u.email
    WHERE fr.to_email = :email AND fr.status = 'PENDING'
    """)
    Flux<FriendPendingResponse> findReceivedRequests(String email);

    @Query("""
    SELECT COUNT(*) > 0 
    FROM friend_request 
    WHERE status = :status 
      AND ((from_email = :email1 AND to_email = :email2) OR (from_email = :email2 AND to_email = :email1))
    """)
    Mono<Boolean> existsPendingBetween(String email1, String email2, FriendRequestStatus status);
}