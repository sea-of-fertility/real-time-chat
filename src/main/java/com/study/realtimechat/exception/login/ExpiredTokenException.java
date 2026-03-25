package com.study.realtimechat.exception.login;

import com.study.realtimechat.exception.ErrorCode;

public class ExpiredTokenException extends LoginException {

    public ExpiredTokenException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
