package com.study.realtimechat.repository;

import com.study.realtimechat.model.entity.FriendInvitationEntity;
import com.study.realtimechat.model.enums.FriendInvitationStatus;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Repository
public interface FriendShipSendRepository extends ReactiveCrudRepository<FriendInvitationEntity, Long> {
    Mono<Boolean>existsByFromEmailAndToEmailAndStatus(String from, String to, FriendInvitationStatus status);
    Flux<FriendInvitationEntity> findByToEmailAndStatus(String to, FriendInvitationStatus status);
    Flux<FriendInvitationEntity>findByFromEmailAndStatus(String from, FriendInvitationStatus status);

    Mono<FriendInvitationEntity> findByFromEmailAndToEmailAndStatus(String from, String to, FriendInvitationStatus status);

    @Query("""
    SELECT fr.id AS requestId, fr.from_email AS fromEmail, u.nickname AS fromNickname, fr.created_at AS createdAt
    FROM friend_invitation fr
    JOIN users u ON fr.from_email = u.email
    WHERE fr.to_email = :email AND fr.status = 'PENDING'
    """)
    Flux<FriendPendingResponse> findReceivedRequests(String email);

    @Query("""
    SELECT COUNT(*) > 0 
    FROM friend_invitation 
    WHERE status = :status 
      AND ((from_email = :email1 AND to_email = :email2) OR (from_email = :email2 AND to_email = :email1))
    """)
    Mono<Boolean> existsPendingBetween(String email1, String email2, FriendInvitationStatus status);
}