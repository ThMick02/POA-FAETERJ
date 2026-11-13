package com.escambo.bancotempo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BancoTempoApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoTempoApplication.class, args);
    }
}
