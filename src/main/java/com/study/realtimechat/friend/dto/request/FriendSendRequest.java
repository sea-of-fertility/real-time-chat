package com.study.realtimechat.friend.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record FriendSendRequest(
        @NotEmpty
        String email
) {

}
