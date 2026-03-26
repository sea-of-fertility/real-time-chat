package com.study.realtimechat.user.domain.request;

import com.study.realtimechat.user.domain.enums.FriendAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FriendInvitationRequest(
        @NotBlank
        String email,
        @NotNull
        FriendAction action
) {
}
