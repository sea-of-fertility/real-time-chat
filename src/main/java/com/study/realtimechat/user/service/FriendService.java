package com.study.realtimechat.user.service;


import com.study.realtimechat.exception.ErrorCode;
import com.study.realtimechat.exception.user.DuplicatedRequest;
import com.study.realtimechat.model.entity.FriendRequestEntity;
import com.study.realtimechat.model.enums.FriendRequestStatus;
import com.study.realtimechat.repository.FriendShipSendRepository;
import com.study.realtimechat.repository.FriendShipRepository;
import com.study.realtimechat.user.domain.FriendShipSendResponse;
import com.study.realtimechat.user.domain.mapper.FriendShipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendShipSendRepository friendShipSendRepository;
    private final FriendShipRepository friendShipRepository;
    private final FriendShipMapper friendShipMapper;

    public Mono<FriendShipSendResponse> sendRequest(String fromEmail, String toEmail) {
        if(fromEmail.equals(toEmail)){
            return Mono.error(new DuplicatedRequest(ErrorCode.SELF_FRIEND_REQUEST));
        }

        return friendShipRepository.existsBetween(fromEmail, toEmail)
                .flatMap(isFriend -> {
                    if (isFriend) {
                        return Mono.error(new DuplicatedRequest(ErrorCode.ALREADY_FRIEND));
                    }
                    return friendShipSendRepository.existsPendingBetween(fromEmail, toEmail, FriendRequestStatus.PENDING);
                })
                .flatMap(hasPending -> {
                    if (hasPending) {
                        return Mono.error(new DuplicatedRequest(ErrorCode.FRIEND_REQUEST_ALREADY_SENT));
                    }
                    return friendShipSendRepository.save(FriendRequestEntity.builder()
                                    .fromEmail(fromEmail)
                                    .toEmail(toEmail)
                                    .status(FriendRequestStatus.PENDING)
                                    .build())
                            .map(friendShipMapper::toFriendShipSendResponse);
                });
    }
}
