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
        if (!service.register(d)) {
            String message = "Email already used or password does not meet requirements. " + 
                           service.getPasswordValidationMessage();
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, message));
        }
        return ResponseEntity.ok(new ApiResponseDto(true, "Registration successful"));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto d) {
        var resp = service.login(d);
        if (resp == null)
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Invalid credentials"));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenDto d) {
        String newToken = service.refreshToken(d.refreshToken());
        if (newToken == null)
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Invalid refresh token"));
        return ResponseEntity.ok(new TokenResponseDto(newToken));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponseDto> logout() {
        // In a real implementation, you would blacklist the token
        return ResponseEntity.ok(new ApiResponseDto(true, "Logged out successfully"));
    }

    @GetMapping("/api/auth/verify")
    public ResponseEntity<ApiResponseDto> verify() {
        return ResponseEntity.ok(new ApiResponseDto(true, null));
    }

    @GetMapping("/api/auth/password-requirements")
    public ResponseEntity<ApiResponseDto> getPasswordRequirements() {
        return ResponseEntity.ok(new ApiResponseDto(true, service.getPasswordValidationMessage()));
    }

    // DTOs for new endpoints
    public record RefreshTokenDto(String refreshToken) {}
    public record TokenResponseDto(String token) {}
}
