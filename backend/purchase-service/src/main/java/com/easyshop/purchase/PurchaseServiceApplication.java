package com.easyshop.purchase;

import com.easyshop.common.security.SecurityConfig;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients
@Import(SecurityConfig.class)
public class PurchaseServiceApplication {
    public static void main(String[] a) {
        SpringApplication.run(PurchaseServiceApplication.class, a);
    }
}
