package com.study.realtimechat.user.controller;

import com.study.realtimechat.auth.model.request.FriendSendRequest;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import com.study.realtimechat.user.domain.response.FriendShipSendResponse;
import com.study.realtimechat.user.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/friend")
    public Mono<FriendShipSendResponse> sendFriendRequest(@AuthenticationPrincipal String email,
                                                          @Valid @RequestBody FriendSendRequest request)
    {
        return friendService.sendRequest(email, request.email());
    }

    @GetMapping("/friend/pending")
    public Flux<FriendPendingResponse> getReceivedFriendRequests(@AuthenticationPrincipal String email) {
        return friendService.getReceivedFriendRequests(email);
    }
}
