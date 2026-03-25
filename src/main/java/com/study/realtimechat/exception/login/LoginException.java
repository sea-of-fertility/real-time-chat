package com.study.realtimechat.exception.login;

public abstract class LoginException extends RuntimeException{
    protected LoginException() {
        super();
    }

    protected LoginException(String message) {
        super(message);
    }
}
