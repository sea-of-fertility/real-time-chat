package com.study.realtimechat.global.exception;

import com.study.realtimechat.global.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, ServerWebExchange exchange) {
        Locale locale = resolveLocale(exchange);
        ErrorCode errorCode = ex.getErrorCode();
        String message = messageSource.getMessage(errorCode.getMessageKey(), null, locale);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.name(), message));
    }

    private Locale resolveLocale(ServerWebExchange exchange) {
        var locales = exchange.getRequest().getHeaders().getAcceptLanguageAsLocales();
        return locales.isEmpty() ? Locale.ENGLISH : locales.getFirst();
    }
}
