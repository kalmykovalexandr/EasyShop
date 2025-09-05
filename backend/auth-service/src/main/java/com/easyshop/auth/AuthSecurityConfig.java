package com.easyshop.auth;

import org.springframework.context.annotation.Configuration;

/**
 * Auth Service does not require security configuration.
 * All authentication checks are performed in API Gateway.
 * Auth Service only creates JWT tokens.
 */
@Configuration
public class AuthSecurityConfig {
    // Auth Service does not require additional security configuration
    // All endpoints are accessible, validation is performed in API Gateway
}
