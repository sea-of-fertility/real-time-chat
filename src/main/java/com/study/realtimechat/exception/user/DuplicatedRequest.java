package com.study.realtimechat.exception.user;

import com.study.realtimechat.exception.CustomException;
import com.study.realtimechat.exception.ErrorCode;

public class DuplicatedRequest extends CustomException {
    public DuplicatedRequest(ErrorCode errorCode) {
        super(errorCode);
    }
}
