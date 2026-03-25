package com.study.realtimechat.auth.model.request;

public record SignupRequest(
        String name,
        String email,
        String password){
}
