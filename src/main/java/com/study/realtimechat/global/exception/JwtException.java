package com.study.realtimechat.global.exception;

public class JwtException extends CustomException {
    public JwtException() {
        super(ErrorCode.JWT_ERROR);
    }
}
