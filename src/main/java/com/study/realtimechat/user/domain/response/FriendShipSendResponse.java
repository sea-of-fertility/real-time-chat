package com.study.realtimechat.user.domain.response;

import com.study.realtimechat.model.enums.FriendRequestStatus;

public record FriendShipSendResponse(
        Long requestId,
        FriendRequestStatus requestStatus
) {
}
