package com.study.realtimechat.friend.dto.response;

import com.study.realtimechat.friend.enums.FriendInvitationStatus;

public record FriendShipSendResponse(
        Long requestId,
        FriendInvitationStatus requestStatus
) {
}
