package com.easyshop.auth.web;

import com.easyshop.auth.jwt.*;
import com.easyshop.auth.user.*;
import jakarta.validation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.password.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    public Map<String, Object> h() {
        return Map.of("ok", true);
    }

    @GetMapping("/readyz")
    public Map<String, Object> r() {
        return Map.of("ok", true);
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> reg(@Valid @RequestBody AuthDto d) {
        if (users.existsByEmail(d.email()))
            return ResponseEntity.badRequest().body(Map.of("message", "Email already used"));
        users.save(User.builder().email(d.email()).passwordHash(enc.encode(d.password())).role("USER").build());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto d) {
        var u = users.findByEmail(d.email()).orElse(null);
        if (u == null || !enc.matches(d.password(), u.getPasswordHash()))
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
        String t = jwt.create(u.getEmail(), u.getRole());
        return ResponseEntity.ok(Map.of("token", t, "email", u.getEmail(), "role", u.getRole()));
    }

    @GetMapping("/api/auth/verify")
    public ResponseEntity<?> v() {
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
