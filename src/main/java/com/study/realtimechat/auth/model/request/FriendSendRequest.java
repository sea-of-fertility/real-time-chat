package com.study.realtimechat.auth.model.request;

import jakarta.validation.constraints.NotEmpty;

public record FriendSendRequest(
        @NotEmpty
        String email
) {

}
