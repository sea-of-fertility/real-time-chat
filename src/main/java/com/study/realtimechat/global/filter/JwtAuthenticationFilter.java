package com.study.realtimechat.global.filter;

import com.study.realtimechat.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {
    private final JwtProvider jwtProvider;

    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String accessToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (accessToken == null) {
            return chain.filter(exchange);
        }

        if(!accessToken.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }
        accessToken = accessToken.substring(7);

        jwtProvider.validateToken(accessToken);

        String email = jwtProvider.extractEmail(accessToken);
        var auth = new UsernamePasswordAuthenticationToken(email, null, List.of());

        return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}