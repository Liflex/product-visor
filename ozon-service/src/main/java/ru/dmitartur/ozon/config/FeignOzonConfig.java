package ru.dmitartur.ozon.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dmitartur.common.security.CompanyContextHolder;
import ru.dmitartur.library.marketplace.entity.CompanyCredentials;
import ru.dmitartur.library.marketplace.service.CompanyCredentialsService;
import java.util.List;
import java.util.UUID;

@Configuration
public class FeignOzonConfig {
    @Bean
    public RequestInterceptor ozonAuthHeadersInterceptor(OzonProperties properties, CompanyCredentialsService credentialsService) {
        return tpl -> {
            // Set defaults first
            tpl.header("Client-Id", properties.getClientId());
            tpl.header("Api-Key", properties.getApiKey());

            // Override with company-specific credentials when available
            String companyId = CompanyContextHolder.getCompanyId();
            if (companyId != null) {
                try {
                    List<CompanyCredentials> credentials = credentialsService.findByCompanyId(UUID.fromString(companyId));
                    if (!credentials.isEmpty()) {
                        CompanyCredentials cc = credentials.get(0); // Берем первый (активный)
                        tpl.header("Client-Id", cc.getClientId());
                        tpl.header("Api-Key", cc.getApiKey());
                    }
                } catch (Exception ignored) {}
            }

            tpl.header("Content-Type", "application/json");
        };
    }
}


