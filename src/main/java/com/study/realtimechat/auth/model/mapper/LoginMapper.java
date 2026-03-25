package com.study.realtimechat.auth.model.mapper;

import com.study.realtimechat.auth.model.request.LoginRequest;
import com.study.realtimechat.auth.model.request.SignupRequest;
import com.study.realtimechat.model.entity.User;
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
