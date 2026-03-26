package com.study.realtimechat.global.exception;

public class ExpiredTokenException extends CustomException {
    public ExpiredTokenException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
