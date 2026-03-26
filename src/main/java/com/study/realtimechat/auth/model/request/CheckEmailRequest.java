package com.study.realtimechat.auth.model.request;

import jakarta.validation.constraints.NotBlank;

public record CheckEmailRequest(
        @NotBlank
        String email
) {
}
