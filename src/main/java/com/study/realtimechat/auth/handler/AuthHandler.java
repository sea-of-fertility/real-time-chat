package com.study.realtimechat.auth.handler;

import com.study.realtimechat.auth.model.request.LoginRequest;
import com.study.realtimechat.auth.model.request.SignupRequest;
import com.study.realtimechat.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthService authService;

    public Mono<ServerResponse> signup(ServerRequest request) {
        return request.bodyToMono(SignupRequest.class)
                .flatMap(authService::signup)
                .flatMap(response -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(response));
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(authService::login)
                .flatMap(reponse -> ServerResponse
                        .ok()
                        .bodyValue(reponse));
    }
}
