package com.study.realtimechat.exception.login;

import com.study.realtimechat.exception.ErrorCode;

public class JwtException extends LoginException {

    public JwtException() {
        super(ErrorCode.JWT_ERROR);
    }
}
