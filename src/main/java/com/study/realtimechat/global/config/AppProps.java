package com.study.realtimechat.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProps {

    private Jwt jwt;

    @Getter
    @Setter
    public static class Jwt {
        private String secretKey;
        private Long accessToken;
        private Long accessTokenExpiration;
        private Long refreshToken;
        private Long refreshTokenExpiration;
    }
}
