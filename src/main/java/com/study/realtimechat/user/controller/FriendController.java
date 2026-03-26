package com.study.realtimechat.user.controller;

import com.study.realtimechat.auth.model.request.FriendSendRequest;
import com.study.realtimechat.user.domain.request.FriendInvitationRequest;
import com.study.realtimechat.user.domain.response.FriendListResponse;
import com.study.realtimechat.user.domain.response.FriendPendingResponse;
import com.study.realtimechat.user.domain.response.FriendShipSendResponse;
import com.study.realtimechat.user.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping
    public Flux<FriendListResponse> getFriendList(@AuthenticationPrincipal String email){
        return friendService.getFriendList(email);
    }

    @PostMapping("/invitations")
    public Mono<FriendShipSendResponse> sendFriendInvitation(@AuthenticationPrincipal String email,
                                                          @Valid @RequestBody FriendSendRequest request) {
        return friendService.sendRequest(email, request.email());
    }

    @GetMapping("/invitations")
    public Flux<FriendPendingResponse> getReceivedFriendInvitations(@AuthenticationPrincipal String email) {
        return friendService.getReceivedFriendInvitations(email);
    }

    @PutMapping("/invitations")
    public Mono<FriendShipSendResponse> respondToInvitation(@AuthenticationPrincipal String email,
                                                            @Valid @RequestBody FriendInvitationRequest request) {
        return friendService.respondToRequest(email, request);
    }
}
