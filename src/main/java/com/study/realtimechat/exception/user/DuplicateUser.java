package com.study.realtimechat.exception.user;

import com.study.realtimechat.exception.CustomException;
import com.study.realtimechat.exception.ErrorCode;

public class DuplicateUser extends CustomException {
    public DuplicateUser() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
