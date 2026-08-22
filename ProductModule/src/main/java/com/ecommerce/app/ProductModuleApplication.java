package com.ecommerce.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.http.HttpHeaders;

@SpringBootApplication
public class ProductModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductModuleApplication.class, args);
    }

}
