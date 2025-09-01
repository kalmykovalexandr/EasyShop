package com.easyshop.auth.web;

import com.easyshop.common.web.ApiResponseDto;
import com.easyshop.auth.service.AuthService;
import jakarta.validation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @GetMapping("/healthz")
    public ApiResponseDto health() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/readyz")
    public ApiResponseDto ready() {
        return new ApiResponseDto(true, null);
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponseDto> register(@Valid @RequestBody AuthDto d) {
        if (!service.register(d))
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Email already used"));
        return ResponseEntity.ok(new ApiResponseDto(true, null));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto d) {
        var resp = service.login(d);
        if (resp == null)
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Invalid credentials"));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/api/auth/verify")
    public ResponseEntity<ApiResponseDto> verify() {
        return ResponseEntity.ok(new ApiResponseDto(true, null));
    }
}
