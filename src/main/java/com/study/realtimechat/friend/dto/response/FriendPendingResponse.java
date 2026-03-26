package com.study.realtimechat.friend.dto.response;

import java.time.Instant;

public record FriendPendingResponse(
        Long requestId,
        String fromEmail,
        String fromNickname,
        Instant createdAt

) {
}
