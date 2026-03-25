package com.study.realtimechat.exception.login;

import com.study.realtimechat.exception.CustomException;
import com.study.realtimechat.exception.ErrorCode;

public abstract class LoginException extends CustomException {

    protected LoginException(ErrorCode errorCode) {
        super(errorCode);
    }
}
