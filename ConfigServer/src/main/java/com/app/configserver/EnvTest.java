package com.app.configserver;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class EnvTest implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("POSTGRES_URL = " + System.getenv("POSTGRES_URL"));
        System.out.println("MONGODB_URL = " + System.getenv("MONGODB_URL"));
        System.out.println("RABBITMQ_HOST = " + System.getenv("RABBITMQ_HOST"));
    }
}