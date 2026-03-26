package com.study.realtimechat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "error.email_already_exists"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "error.invalid_credentials"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "error.token_expired"),
    JWT_ERROR(HttpStatus.UNAUTHORIZED, "error.jwt_error"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.user_not_found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "error.user_already_exists"),


    // Friend
    ALREADY_FRIENDS(HttpStatus.CONFLICT, "error.already_friends"),
    FRIEND_INVITATION_ALREADY_SENT(HttpStatus.CONFLICT, "error.friend_request_already_sent"),
    SELF_FRIEND_INVITATION(HttpStatus.BAD_REQUEST, "error.self_friend_request"),
    ALREADY_FRIEND(HttpStatus.BAD_REQUEST, "error.duplicated_friend_request"),
    FRIEND_INVITATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "error.friend_request_not_found")
    ;
    private final HttpStatus httpStatus;
    private final String messageKey;
}
