package ru.dmitartur.ozon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"ru.dmitartur.ozon", "ru.dmitartur.library.marketplace"})
@EnableFeignClients(basePackages = "ru.dmitartur.ozon.integration")
@EnableScheduling
@EnableRetry
@EnableJpaRepositories(basePackages = {"ru.dmitartur.ozon", "ru.dmitartur.library.marketplace"})
@EntityScan(basePackages = {"ru.dmitartur.ozon", "ru.dmitartur.library.marketplace"})
public class OzonServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OzonServiceApplication.class, args);
    }
}


