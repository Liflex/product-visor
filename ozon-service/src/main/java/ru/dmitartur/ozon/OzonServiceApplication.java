package ru.dmitartur.ozon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dmitartur.ozon.integration")
@EnableScheduling
@EnableRetry
public class OzonServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OzonServiceApplication.class, args);
    }
}


