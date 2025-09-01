package com.easyshop.gateway.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponseDto>> handleValidation(WebExchangeBindException ex) {
        String msg = ex.getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage())
                .orElse("Validation error");
        log.warn("Validation failed: {}", msg);
        return Mono.just(ResponseEntity.badRequest().body(new ApiResponseDto(false, msg)));
    }

    @ExceptionHandler(DataAccessException.class)
    public Mono<ResponseEntity<ApiResponseDto>> handleDatabase(DataAccessException ex) {
        log.error("Database error", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto(false, "Database error")));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponseDto>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto(false, "Unexpected error")));
    }
}

