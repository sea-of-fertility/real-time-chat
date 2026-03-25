package com.study.realtimechat.config;

import com.study.realtimechat.exception.login.ExpiredTokenException;
import com.study.realtimechat.exception.login.JwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {

    private final AppProps appProps;
    private final SecretKey secretKey;

    public JwtProvider(AppProps appProps) {
        this.appProps = appProps;
        this.secretKey = Keys.hmacShaKeyFor(appProps.getJwt().getSecretKey().getBytes());
    }


    public String generateAccessToken(String email) {
        Date dateNow = Date.from(Instant.now());
        Date accessTokenExpired = new Date(dateNow.getTime() + appProps.getJwt().getAccessTokenExpiration());
        return Jwts.builder()
                .subject(email)
                .issuedAt(dateNow)
                .expiration(accessTokenExpired)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String email) {
        Date dateNow = Date.from(Instant.now());
        Date refreshTokenExpired = new Date(dateNow.getTime() + appProps.getJwt().getRefreshTokenExpiration());
        return Jwts.builder()
                .subject(email)
                .issuedAt(dateNow)
                .expiration(refreshTokenExpired)
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    //todo 다국어 처리해야한다. jwt 구현 후에 다국어 세팅할 것
    public void validateToken(String token) {
        try {
            extractEmail(token);
        } catch (ExpiredJwtException _) {
            throw new ExpiredTokenException("token 만기");
        } catch (JwtException _) {
            throw new JwtException("Jwt 예외 발생");
        }
    }
}
