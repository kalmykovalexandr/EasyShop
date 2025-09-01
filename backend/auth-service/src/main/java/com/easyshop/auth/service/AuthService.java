package com.easyshop.auth.service;

import com.easyshop.auth.jwt.JwtService;
import com.easyshop.auth.user.User;
import com.easyshop.auth.user.UserRepository;
import com.easyshop.auth.web.AuthDto;
import com.easyshop.auth.web.LoginResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder enc;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder enc, JwtService jwt) {
        this.users = users;
        this.enc = enc;
        this.jwt = jwt;
    }

    public boolean register(AuthDto d) {
        if (users.existsByEmail(d.email())) {
            return false;
        }
        users.save(User.builder()
                .email(d.email())
                .passwordHash(enc.encode(d.password()))
                .role("USER")
                .build());
        return true;
    }

    public LoginResponseDto login(AuthDto d) {
        var u = users.findByEmail(d.email()).orElse(null);
        if (u == null || !enc.matches(d.password(), u.getPasswordHash())) {
            return null;
        }
        String t = jwt.create(u.getEmail(), u.getRole());
        return new LoginResponseDto(t, u.getEmail(), u.getRole());
    }
}
