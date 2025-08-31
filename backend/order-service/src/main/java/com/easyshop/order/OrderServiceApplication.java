package com.easyshop.order;

import com.easyshop.common.security.SecurityConfig;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecurityConfig.class)
public class OrderServiceApplication {
    public static void main(String[] a) {
        SpringApplication.run(OrderServiceApplication.class, a);
    }
}
