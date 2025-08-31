package com.easyshop.auth.web;

import jakarta.validation.constraints.*;

public record AuthDto(@Email @NotBlank String email, @NotBlank String password) {
}
