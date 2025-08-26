package ru.dmitartur.yandex.retry;

import org.springframework.stereotype.Component;
import ru.dmitartur.library.marketplace.retry.BaseMarketplaceRetryPolicy;

@Component
public class YandexRetryPolicy extends BaseMarketplaceRetryPolicy {

    public YandexRetryPolicy() {
        super("Yandex", 5);
    }

    @Override
    protected boolean isRetryableError(String errorCode, String errorMessage) {
        // Определяем, какие ошибки Yandex Market API можно повторить
        switch (errorCode) {
            case "RATE_LIMIT_EXCEEDED":
            case "TOO_MANY_REQUESTS":
            case "SERVICE_UNAVAILABLE":
            case "INTERNAL_ERROR":
            case "GATEWAY_TIMEOUT":
            case "BAD_GATEWAY":
                return true;
            case "UNAUTHORIZED":
            case "FORBIDDEN":
            case "NOT_FOUND":
            case "BAD_REQUEST":
            case "VALIDATION_ERROR":
                return false;
            default:
                // По умолчанию повторяем для неизвестных ошибок
                return true;
        }
    }
}
