package com.easyshop.auth.web;

public record LoginResponseDto(String token, String refreshToken, String email, String role) {
}
