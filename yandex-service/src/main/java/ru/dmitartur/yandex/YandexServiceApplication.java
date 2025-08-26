package ru.dmitartur.yandex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class YandexServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(YandexServiceApplication.class, args);
    }
}
