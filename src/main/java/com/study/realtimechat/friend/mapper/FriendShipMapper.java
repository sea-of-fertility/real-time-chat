package com.study.realtimechat.friend.mapper;


import com.study.realtimechat.friend.entity.FriendInvitationEntity;
import com.study.realtimechat.friend.entity.FriendShipEntity;
import com.study.realtimechat.friend.dto.response.FriendPendingResponse;
import com.study.realtimechat.friend.dto.response.FriendShipSendResponse;
import org.springframework.stereotype.Component;

@Component
public class FriendShipMapper {

    public FriendShipSendResponse toFriendShipSendResponse(FriendInvitationEntity entity) {
        return new FriendShipSendResponse(entity.getId(), entity.getStatus());
    }

    public FriendPendingResponse toFriendPendingResponse(FriendInvitationEntity entity, String fromNickName) {
        return new FriendPendingResponse(entity.getId(), entity.getFromEmail(), fromNickName, entity.getCreatedAt());
    }

    public FriendShipEntity toFriendShipEntity(FriendInvitationEntity entity) {
        return FriendShipEntity.builder()
                .friendshipA(entity.getFromEmail())
                .friendshipB(entity.getToEmail())
                .build();
    }
}
