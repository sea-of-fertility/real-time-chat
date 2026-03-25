package com.study.realtimechat.exception.user;

public class DuplicateUser extends  RuntimeException {
    public DuplicateUser() {
        super();
    }

    public DuplicateUser(String message) {
        super(message);
    }
}
