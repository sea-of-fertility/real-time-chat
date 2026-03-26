package com.study.realtimechat.auth.controller;

import com.study.realtimechat.auth.dto.request.CheckEmailRequest;
import com.study.realtimechat.auth.dto.request.LoginRequest;
import com.study.realtimechat.auth.dto.request.RefreshTokenRequest;
import com.study.realtimechat.auth.dto.request.SignupRequest;
import com.study.realtimechat.auth.dto.response.LoginResponse;
import com.study.realtimechat.auth.dto.response.RefreshTokenResponse;
import com.study.realtimechat.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<LoginResponse> signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public Mono<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/email")
    public Mono<Boolean> isEmailDuplicated(@Valid @RequestBody CheckEmailRequest request) {
        return authService.isEmailDuplicated(request);
    }
}
