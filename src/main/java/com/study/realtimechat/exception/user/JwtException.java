package com.study.realtimechat.exception.user;

import com.study.realtimechat.exception.CustomException;
import com.study.realtimechat.exception.ErrorCode;

public class JwtException extends CustomException {
    public JwtException() {
        super(ErrorCode.JWT_ERROR);
    }
}
