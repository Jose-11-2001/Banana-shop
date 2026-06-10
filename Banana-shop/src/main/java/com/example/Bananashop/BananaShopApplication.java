package com.example.Bananashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.Bananashop", "com.example.Bananashop.config", "com.example.Bananashop.controller", "com.example.Bananashop.model", "com.example.Bananashop.repository", "com.example.Bananashop.service"})
public class BananaShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(BananaShopApplication.class, args);
    }
}