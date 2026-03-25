package com.study.realtimechat.router;

import com.study.realtimechat.handler.HelloHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class HelloRouter {

    @Bean
    public RouterFunction<ServerResponse> helloRoute(HelloHandler helloHandler) {
        return RouterFunctions.route()
                .GET("/api/hello", helloHandler::hello)
                .build();
    }
}
