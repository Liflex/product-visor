package ru.dmitartur.ozon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.dmitartur.ozon.integration")
@EnableScheduling
public class OzonServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OzonServiceApplication.class, args);
    }
}


