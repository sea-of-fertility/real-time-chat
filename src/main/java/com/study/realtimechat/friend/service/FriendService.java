package com.study.realtimechat.friend.service;


import com.study.realtimechat.global.exception.ErrorCode;
import com.study.realtimechat.global.exception.DuplicatedRequest;
import com.study.realtimechat.friend.entity.FriendInvitationEntity;
import com.study.realtimechat.friend.enums.FriendInvitationStatus;
import com.study.realtimechat.friend.repository.FriendInvitationRepository;
import com.study.realtimechat.friend.repository.FriendShipRepository;
import com.study.realtimechat.auth.repository.UserRepository;
import com.study.realtimechat.friend.enums.FriendAction;
import com.study.realtimechat.friend.mapper.FriendShipMapper;
import com.study.realtimechat.friend.dto.request.FriendInvitationRequest;
import com.study.realtimechat.friend.dto.response.FriendListResponse;
import com.study.realtimechat.friend.dto.response.FriendPendingResponse;
import com.study.realtimechat.friend.dto.response.FriendShipSendResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendShipRepository friendShipRepository;
    private final FriendInvitationRepository friendInvitationRepository;

    private final FriendShipMapper friendShipMapper;

    public Flux<FriendListResponse> getFriendList(String email){
        return friendShipRepository.findFriendList(email);
    }

    public Mono<FriendShipSendResponse> sendRequest(String fromEmail, String toEmail) {
        if(fromEmail.equals(toEmail)){
            return Mono.error(new DuplicatedRequest(ErrorCode.SELF_FRIEND_INVITATION));
        }

        return friendShipRepository.existsBetween(fromEmail, toEmail)
                .flatMap(isFriend -> {
                    if (Boolean.TRUE.equals(isFriend)) {
                        return Mono.error(new DuplicatedRequest(ErrorCode.ALREADY_FRIEND));
                    }
                    return friendInvitationRepository.existsPendingBetween(fromEmail, toEmail, FriendInvitationStatus.PENDING);
                })
                .flatMap(hasPending -> {
                    if (Boolean.TRUE.equals(hasPending)) {
                        return Mono.error(new DuplicatedRequest(ErrorCode.FRIEND_INVITATION_ALREADY_SENT));
                    }
                    return friendInvitationRepository.save(FriendInvitationEntity.builder()
                                    .fromEmail(fromEmail)
                                    .toEmail(toEmail)
                                    .status(FriendInvitationStatus.PENDING)
                                    .build())
                            .map(friendShipMapper::toFriendShipSendResponse);
                });
    }

    public Flux<FriendPendingResponse> getReceivedFriendInvitations(String email) {
        return friendInvitationRepository.findReceivedRequests(email);
    }

    public Mono<FriendShipSendResponse> respondToRequest(String myEmail, FriendInvitationRequest friend) {
        return friendInvitationRepository.findByFromEmailAndToEmailAndStatus(friend.email(), myEmail, FriendInvitationStatus.PENDING)
                .switchIfEmpty(Mono.error(new DuplicatedRequest(ErrorCode.FRIEND_INVITATION_NOT_FOUND)))
                .flatMap(r -> {
                    r.accept(friend.action());
                    Mono<Void> afterSave = friend.action() == FriendAction.ACCEPTED
                            ? friendShipRepository.save(friendShipMapper.toFriendShipEntity(r)).then()
                            : Mono.empty();
                    return friendInvitationRepository.save(r)
                            .then(afterSave)
                            .thenReturn(friendShipMapper.toFriendShipSendResponse(r));
                });
    }
}