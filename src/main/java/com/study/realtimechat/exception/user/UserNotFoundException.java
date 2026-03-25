package com.study.realtimechat.exception.user;

import com.study.realtimechat.exception.CustomException;
import com.study.realtimechat.exception.ErrorCode;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
