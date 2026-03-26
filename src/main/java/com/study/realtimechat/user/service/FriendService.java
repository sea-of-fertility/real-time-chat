package com.study.realtimechat.user.service;


import com.study.realtimechat.exception.ErrorCode;
import com.study.realtimechat.exception.user.DuplicatedRequest;
import com.study.realtimechat.model.entity.FriendInvitationEntity;
import com.study.realtimechat.model.enums.FriendInvitationStatus;
import com.study.realtimechat.repository.FriendInvitationRepository;
import com.study.realtimechat.repository.FriendShipRepository;
import com.study.realtimechat.user.domain.enums.FriendAction;
import com.study.realtimechat.user.domain.mapper.FriendShipMapper;
import com.study.realtimechat.user.domain.request.FriendInvitationRequest;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import com.study.realtimechat.user.domain.response.FriendShipSendResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendShipRepository friendShipRepository;
    private final FriendInvitationRepository friendInvitationRepository;

    private final FriendShipMapper friendShipMapper;

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
        return friendShipSendRepository.findReceivedRequests(email);
    }
}
