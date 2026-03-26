package com.study.realtimechat.auth.controller;

import com.study.realtimechat.auth.dto.request.CheckEmailRequest;
import com.study.realtimechat.auth.dto.request.LoginRequest;
import com.study.realtimechat.auth.dto.request.RefreshTokenRequest;
import com.study.realtimechat.auth.dto.request.SignupRequest;
import com.study.realtimechat.auth.dto.response.LoginResponse;
import com.study.realtimechat.auth.dto.response.RefreshTokenResponse;
import com.study.realtimechat.auth.service.AuthService;
import com.study.realtimechat.global.config.SecurityConfig;
import com.study.realtimechat.global.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@WebFluxTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        )
)
@ExtendWith(RestDocumentationExtension.class)
class AuthControllerDocsTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp(
            @Autowired WebTestClient webTestClient,
            RestDocumentationContextProvider restDocumentation
    ) {
        this.webTestClient = webTestClient.mutate()
                .filter(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    void 회원가입() {
        var request = new SignupRequest("테스트", "test@example.com", "password123");
        var response = new LoginResponse("access-token-example", "refresh-token-example");

        given(authService.signup(any())).willReturn(Mono.just(response));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(document("auth-signup",
                        requestFields(
                                fieldWithPath("name").description("사용자 이름"),
                                fieldWithPath("email").description("이메일 주소"),
                                fieldWithPath("password").description("비밀번호 (8자 이상)")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("액세스 토큰 (30분)"),
                                fieldWithPath("refreshToken").description("리프레시 토큰 (7일)")
                        )
                ));
    }

    @Test
    void 로그인() {
        var request = new LoginRequest("test@example.com", "password123");
        var response = new LoginResponse("access-token-example", "refresh-token-example");

        given(authService.login(any())).willReturn(Mono.just(response));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("auth-login",
                        requestFields(
                                fieldWithPath("email").description("이메일 주소"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("액세스 토큰 (30분)"),
                                fieldWithPath("refreshToken").description("리프레시 토큰 (7일)")
                        )
                ));
    }

    @Test
    void 토큰_갱신() {
        var request = new RefreshTokenRequest("refresh-token-example");
        var response = new RefreshTokenResponse("new-access-token-example");

        given(authService.refreshToken(any())).willReturn(Mono.just(response));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("auth-refresh",
                        requestFields(
                                fieldWithPath("refreshToken").description("리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("accessToken").description("새로 발급된 액세스 토큰")
                        )
                ));
    }

    @Test
    void 이메일_중복_확인() {
        var request = new CheckEmailRequest("test@example.com");

        given(authService.isEmailDuplicated(any())).willReturn(Mono.just(false));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/api/auth/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("auth-check-email",
                        requestFields(
                                fieldWithPath("email").description("중복 확인할 이메일 주소")
                        )
                ));
    }
}
