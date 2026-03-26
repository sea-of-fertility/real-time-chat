package com.study.realtimechat.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckEmailRequest(
        @NotBlank
        String email
) {
}
