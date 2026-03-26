package com.study.realtimechat.global.exception;

public class DuplicatedRequest extends CustomException {
    public DuplicatedRequest(ErrorCode errorCode) {
        super(errorCode);
    }
}
