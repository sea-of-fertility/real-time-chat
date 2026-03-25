package com.study.realtimechat.auth.router;

import com.study.realtimechat.auth.handler.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class AuthRouter {

    @Bean
    public RouterFunction<ServerResponse> authRoute(AuthHandler authHandler) {
        return RouterFunctions.route()
                .path("/api/auth", builder -> builder
                        .POST("/signup", authHandler::signup)
                        .POST("/login", authHandler::login)
                        .POST("/refresh", authHandler::refreshToken))
                .build();
    }
}
