package com.study.realtimechat.exception.login;

public class JwtException extends LoginException{
    public JwtException() {
        super();
    }
    public JwtException(String message) {
        super(message);
    }
}
