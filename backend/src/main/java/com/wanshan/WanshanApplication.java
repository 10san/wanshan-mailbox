package com.wanshan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WanshanApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanshanApplication.class, args);
        System.out.println("🌙 晚山信箱已启动 — http://localhost:8080");
    }
}
