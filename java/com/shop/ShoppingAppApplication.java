package com.shop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 溜溜商城后端启动类。
 */
@SpringBootApplication
@MapperScan("com.shop.mapper")
public class ShoppingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingAppApplication.class, args);
    }
}
