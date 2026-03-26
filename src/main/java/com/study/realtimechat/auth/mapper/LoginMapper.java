package com.study.realtimechat.auth.mapper;

import com.study.realtimechat.auth.dto.request.LoginRequest;
import com.study.realtimechat.auth.dto.request.SignupRequest;
import com.study.realtimechat.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class LoginMapper {

    public LoginRequest toLoginRequest(SignupRequest signupRequest) {
        return new LoginRequest(signupRequest.email(), signupRequest.password());
    }

    public User toUser(SignupRequest signupRequest) {
        return User.builder()
                .email(signupRequest.email())
                .password(signupRequest.password())
                .nickname(signupRequest.name())
                .build();
    }
}
