package com.study.realtimechat.global.exception;

public class DuplicateUser extends CustomException {
    public DuplicateUser() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
}
