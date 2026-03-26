package com.study.realtimechat.friend.dto.request;

import com.study.realtimechat.friend.enums.FriendAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FriendInvitationRequest(
        @NotBlank
        String email,
        @NotNull
        FriendAction action
) {
}
