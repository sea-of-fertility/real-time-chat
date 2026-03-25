package com.study.realtimechat.exception.user;

import com.study.realtimechat.exception.CustomException;
import com.study.realtimechat.exception.ErrorCode;

public class ExpiredTokenException extends CustomException {
    public ExpiredTokenException() {
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
