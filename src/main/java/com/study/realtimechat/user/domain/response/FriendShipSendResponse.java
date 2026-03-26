package com.study.realtimechat.user.domain.response;

import com.study.realtimechat.model.enums.FriendInvitationStatus;

public record FriendShipSendResponse(
        Long requestId,
        FriendInvitationStatus requestStatus
) {
}
