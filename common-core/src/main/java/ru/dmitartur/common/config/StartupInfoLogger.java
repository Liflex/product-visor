package ru.dmitartur.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
public class StartupInfoLogger implements CommandLineRunner {

    private final Environment env;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:application}")
    private String appName;

    public StartupInfoLogger(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) {
        log.info("================= {} STARTED =================", appName);
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        log.info("Server port: {}", serverPort);
        log.info("Swagger UI: http://localhost:{}/swagger-ui.html", serverPort);
        log.info("--- All properties (non-sensitive) ---");
        for (var propertyName : ((org.springframework.core.env.AbstractEnvironment) env).getPropertySources()) {
            if (propertyName instanceof org.springframework.core.env.EnumerablePropertySource<?> eps) {
                for (String name : eps.getPropertyNames()) {
                    if (!name.toLowerCase().contains("password") && !name.toLowerCase().contains("secret")) {
                        log.info("{} = {}", name, env.getProperty(name));
                    }
                }
            }
        }
        log.info("================================================");
    }
}
