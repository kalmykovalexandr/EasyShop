package com.easyshop.auth;

import com.easyshop.common.security.SecurityConfig;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecurityConfig.class)
public class AuthServiceApplication {
    public static void main(String[] a) {
        SpringApplication.run(AuthServiceApplication.class, a);
    }
}
