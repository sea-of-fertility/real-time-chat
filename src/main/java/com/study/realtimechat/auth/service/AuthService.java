package com.study.realtimechat.auth.service;

import com.study.realtimechat.auth.model.mapper.LoginMapper;
import com.study.realtimechat.auth.model.request.LoginRequest;
import com.study.realtimechat.auth.model.request.RefreshTokenRequest;
import com.study.realtimechat.auth.model.request.SignupRequest;
import com.study.realtimechat.auth.model.response.LoginResponse;
import com.study.realtimechat.auth.model.response.RefreshTokenResponse;
import com.study.realtimechat.auth.repository.UserRepository;
import com.study.realtimechat.config.JwtProvider;
import com.study.realtimechat.exception.user.DuplicateUser;
import com.study.realtimechat.exception.user.UserNotFoundException;
import com.study.realtimechat.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.study.realtimechat.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final LoginMapper loginMapper;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public Mono<LoginResponse> signup(SignupRequest request) {
        return userRepository.existsByEmail(request.email())
                .flatMap(exits -> {
                    if (exits) {
                        return Mono.error(new DuplicateUser());
                    }

                    User user = loginMapper.toUser(request);
                    String encodedPassword = passwordEncoder.encode(request.password());
                    user.encodePassword(encodedPassword);

                    return userRepository.save(user)
                            .then(login(loginMapper.toLoginRequest(request)));
                });
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .switchIfEmpty(Mono.error(new UserNotFoundException(USER_NOT_FOUND)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
                        return Mono.error(new UserNotFoundException(USER_NOT_FOUND));
                    }
                    LoginResponse loginResponse = new LoginResponse(jwtProvider.generateAccessToken(user.getEmail()), jwtProvider.generateRefreshToken(user.getEmail()));
                    return Mono.just(loginResponse);
                });
    }

    public Mono<RefreshTokenResponse> refreshToken(RefreshTokenRequest request) {
        String token = request.refreshToken();
        jwtProvider.validateToken(token);
        String email = jwtProvider.extractEmail(token);
        String newAccessToken = jwtProvider.generateAccessToken(email);
        return Mono.just(new RefreshTokenResponse(newAccessToken));
    }
}