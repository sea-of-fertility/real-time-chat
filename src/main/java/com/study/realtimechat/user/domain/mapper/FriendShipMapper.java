package com.study.realtimechat.user.domain.mapper;


import com.study.realtimechat.model.entity.FriendRequestEntity;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import com.study.realtimechat.user.domain.response.FriendShipSendResponse;
import org.springframework.stereotype.Component;

@Component
public class FriendShipMapper {

    public FriendShipSendResponse toFriendShipSendResponse(FriendRequestEntity entity) {
        return new FriendShipSendResponse(entity.getId(), entity.getStatus());
    }

    public FriendPendingResponse toFriendPendingResponse(FriendRequestEntity entity, String fromNickName) {
        return new FriendPendingResponse(entity.getId(), entity.getFromEmail(), fromNickName, entity.getCreatedAt());
    }

}
