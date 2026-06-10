package com.example.Banana_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bananashop", "com.example.Banana_shop"})
public class BananaShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(BananaShopApplication.class, args);
    }
}