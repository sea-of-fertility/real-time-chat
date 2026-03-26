package com.study.realtimechat.user.domain.response;

import java.time.Instant;

public record FriendPendingResponse(
        Long requestId,
        String fromEmail,
        String fromNickname,
        Instant createdAt

) {
}
