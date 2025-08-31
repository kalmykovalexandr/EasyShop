package com.easyshop.auth.web;

public record LoginResponseDto(String token, String email, String role) {
}
