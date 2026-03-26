package com.study.realtimechat.user.controller;

import com.study.realtimechat.auth.model.request.FriendSendRequest;
import com.study.realtimechat.config.SecurityConfig;
import com.study.realtimechat.filter.JwtAuthenticationFilter;
import com.study.realtimechat.model.enums.FriendRequestStatus;
import com.study.realtimechat.user.domain.response.FriendShipSendResponse;
import com.study.realtimechat.user.service.FriendService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;

@WebFluxTest(
        controllers = FriendController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        )
)
@ExtendWith(RestDocumentationExtension.class)
class FriendControllerTest {

    private WebTestClient webTestClient;

    @MockitoBean
    private FriendService friendService;

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
    void 친구_요청() {
        // given
        var request = new FriendSendRequest("friend@example.com");
        var response = new FriendShipSendResponse(1L, FriendRequestStatus.PENDING);

        given(friendService.sendRequest(any(), any()))
                .willReturn(Mono.just(response));

        // when & then
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post().uri("/friend")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("friend-request",
                        requestFields(
                                fieldWithPath("email").description("친구 요청할 대상 이메일")
                        ),
                        responseFields(
                                fieldWithPath("requestId").description("친구 요청 ID"),
                                fieldWithPath("requestStatus").description("요청 상태 (PENDING)")
                        )
                ));
    }

    @Test
    void 받은_친구_요청_목록_조회() {
        // given
        var response1 = new FriendPendingResponse(1L, "user1@example.com", "유저1", Instant.parse("2026-03-26T10:00:00Z"));
        var response2 = new FriendPendingResponse(2L, "user2@example.com", "유저2", Instant.parse("2026-03-26T11:00:00Z"));

        given(friendService.getReceivedFriendRequests(any()))
                .willReturn(Flux.just(response1, response2));

        // when & then
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth))
                .get().uri("/friend/pending")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(document("friend-pending-list",
                        responseFields(
                                fieldWithPath("[].requestId").description("친구 요청 ID"),
                                fieldWithPath("[].fromEmail").description("요청 보낸 사용자 이메일"),
                                fieldWithPath("[].fromNickname").description("요청 보낸 사용자 닉네임"),
                                fieldWithPath("[].createdAt").description("요청 시각 (ISO 8601)")
                        )
                ));
    }
}
