package com.easyshop.product;

import com.easyshop.common.security.SecurityConfig;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecurityConfig.class)
public class ProductServiceApplication {
    public static void main(String[] a) {
        SpringApplication.run(ProductServiceApplication.class, a);
    }
}
