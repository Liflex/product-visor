package ru.dmitartur.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan(basePackages = "ru.dmitartur.common")
@EnableConfigurationProperties({OAuth2ClientCredentialsProperties.class, CoreProperties.class})
@Import({SecurityConfig.class, CorsCommonConfig.class})
public class CoreAutoConfiguration { }



