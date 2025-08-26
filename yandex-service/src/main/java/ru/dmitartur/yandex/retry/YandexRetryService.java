package ru.dmitartur.yandex.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.retry.BaseRetryService;
import ru.dmitartur.yandex.integration.YandexApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexRetryService extends BaseRetryService {

    private final YandexRetryPolicy retryPolicy;

    public YandexRetryService() {
        super("Yandex", new YandexRetryPolicy());
        this.retryPolicy = new YandexRetryPolicy();
    }

    public JsonNode executeWithRetry(ApiOperation operation, String operationName) {
        try {
            log.info("🔄 Executing {} operation for Yandex", operationName);
            JsonNode result = operation.execute();
            logSuccess(operationName);
            return result;
        } catch (Exception e) {
            logFailure(operationName, e.getMessage());
            throw e;
        }
    }

    public JsonNode executeUpdateStocksWithRetry(ApiOperation operation, String offerId) {
        try {
            log.info("🔄 Executing updateStocks operation for Yandex, offerId: {}", offerId);
            JsonNode result = operation.execute();
            logSuccess("updateStocks for " + offerId);
            return result;
        } catch (Exception e) {
            logFailure("updateStocks for " + offerId, e.getMessage());
            throw e;
        }
    }

    @Override
    protected YandexApiException createApiException(JsonNode response) {
        String errorCode = response.path("error").path("code").asText("UNKNOWN_ERROR");
        String errorMessage = response.path("error").path("message").asText("Unknown error");
        return new YandexApiException(errorCode, errorMessage);
    }

    @FunctionalInterface
    public interface ApiOperation {
        JsonNode execute() throws Exception;
    }
}
