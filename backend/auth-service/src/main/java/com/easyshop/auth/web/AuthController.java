package com.easyshop.auth.web;

import com.easyshop.auth.jwt.*;
import com.easyshop.auth.user.*;
import jakarta.validation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder enc;
    private final JwtService jwt;

    public AuthController(UserRepository u, PasswordEncoder e, JwtService j) {
        users = u;
        enc = e;
        jwt = j;
    }

    @GetMapping("/healthz")
    public ApiResponseDto h() {
        return new ApiResponseDto(true, null);
    }

    @GetMapping("/readyz")
    public ApiResponseDto r() {
        return new ApiResponseDto(true, null);
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponseDto> reg(@Valid @RequestBody AuthDto d) {
        if (users.existsByEmail(d.email()))
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Email already used"));
        users.save(User.builder().email(d.email()).passwordHash(enc.encode(d.password())).role("USER").build());
        return ResponseEntity.ok(new ApiResponseDto(true, null));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto d) {
        var u = users.findByEmail(d.email()).orElse(null);
        if (u == null || !enc.matches(d.password(), u.getPasswordHash()))
            return ResponseEntity.badRequest().body(new ApiResponseDto(false, "Invalid credentials"));
        String t = jwt.create(u.getEmail(), u.getRole());
        return ResponseEntity.ok(new LoginResponseDto(t, u.getEmail(), u.getRole()));
    }

    @GetMapping("/api/auth/verify")
    public ResponseEntity<ApiResponseDto> v() {
        return ResponseEntity.ok(new ApiResponseDto(true, null));
    }
}
