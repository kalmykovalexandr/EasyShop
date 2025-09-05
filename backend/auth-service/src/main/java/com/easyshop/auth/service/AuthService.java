package com.easyshop.auth.service;

import com.easyshop.auth.jwt.JwtService;
import com.easyshop.auth.user.User;
import com.easyshop.auth.user.UserRepository;
import com.easyshop.auth.web.AuthDto;
import com.easyshop.auth.web.LoginResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder enc;
    private final JwtService jwt;

    // Password validation patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    public AuthService(UserRepository users, PasswordEncoder enc, JwtService jwt) {
        this.users = users;
        this.enc = enc;
        this.jwt = jwt;
    }

    public boolean register(AuthDto d) {
        if (users.existsByEmail(d.email())) {
            return false;
        }
        
        // Validate password strength
        if (!isValidPassword(d.password())) {
            return false;
        }
        
        users.save(User.builder()
                .email(d.email().toLowerCase().trim())
                .passwordHash(enc.encode(d.password()))
                .role("USER")
                .build());
        return true;
    }

    public LoginResponseDto login(AuthDto d) {
        var u = users.findByEmail(d.email().toLowerCase().trim()).orElse(null);
        if (u == null || !enc.matches(d.password(), u.getPasswordHash())) {
            return null;
        }
        
        String accessToken = jwt.create(u.getEmail(), u.getRole());
        String refreshToken = jwt.createRefreshToken(u.getEmail());
        
        return new LoginResponseDto(accessToken, refreshToken, u.getEmail(), u.getRole());
    }

    public boolean validateToken(String token) {
        return jwt.validateToken(token);
    }

    public String refreshToken(String refreshToken) {
        if (!jwt.validateToken(refreshToken)) {
            return null;
        }
        
        String email = jwt.getSubject(refreshToken);
        var user = users.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        
        return jwt.create(user.getEmail(), user.getRole());
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public String getPasswordValidationMessage() {
        return "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)";
    }
}
