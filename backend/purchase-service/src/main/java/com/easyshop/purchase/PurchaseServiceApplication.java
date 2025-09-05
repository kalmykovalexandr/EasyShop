package com.easyshop.purchase;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PurchaseServiceApplication {
    public static void main(String[] a) {
        SpringApplication.run(PurchaseServiceApplication.class, a);
    }
}
