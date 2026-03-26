package com.study.realtimechat.user.domain.mapper;


import com.study.realtimechat.model.entity.FriendRequestEntity;
import com.study.realtimechat.user.domain.FriendShipSendResponse;
import org.springframework.stereotype.Component;

@Component
public class FriendShipMapper {

    public FriendShipSendResponse toFriendShipSendResponse(FriendRequestEntity entity) {
        return new FriendShipSendResponse(entity.getId(), entity.getStatus());
    }

}
