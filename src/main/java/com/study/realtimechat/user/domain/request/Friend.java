package com.study.realtimechat.user.domain.request;

import com.study.realtimechat.user.domain.enums.FriendAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Friend(
        @NotBlank
        String email,
        @NotNull
        FriendAction action
) {
}
