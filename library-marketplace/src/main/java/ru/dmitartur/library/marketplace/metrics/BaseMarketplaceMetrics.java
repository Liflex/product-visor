package ru.dmitartur.library.marketplace.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

/**
 * Базовый класс для метрик маркетплейсов
 * Предоставляет общие методы для сбора метрик
 */
@Component
public abstract class BaseMarketplaceMetrics {
    
    protected final MeterRegistry meterRegistry;
    protected final String marketplaceName;
    
    public BaseMarketplaceMetrics(MeterRegistry meterRegistry, String marketplaceName) {
        this.meterRegistry = meterRegistry;
        this.marketplaceName = marketplaceName;
    }
    
    /**
     * Записать количество обработанных записей
     */
    public void recordUpserted(int count) {
        if (count <= 0) return;
        meterRegistry.counter(getMetricName("backfill_upserted_total")).increment(count);
    }
    
    /**
     * Записать информацию о batch операции
     */
    public void recordBatch(String result) {
        Tags tags = Tags.of(Tag.of("result", result == null ? "ok" : result));
        meterRegistry.counter(getMetricName("backfill_batches_total"), tags).increment();
    }
    
    /**
     * Записать ошибку
     */
    public void recordError(String stage) {
        Tags tags = Tags.of(Tag.of("stage", stage == null ? "unknown" : stage));
        meterRegistry.counter(getMetricName("backfill_errors_total"), tags).increment();
    }
    
    /**
     * Записать время выполнения операции
     */
    public void recordExecutionTime(String operation, long timeMs) {
        meterRegistry.timer(getMetricName("execution_time"), 
                          Tags.of(Tag.of("operation", operation))).record(java.time.Duration.ofMillis(timeMs));
    }
    
    /**
     * Записать количество API запросов
     */
    public void recordApiRequest(String endpoint, String status) {
        Tags tags = Tags.of(
            Tag.of("endpoint", endpoint),
            Tag.of("status", status)
        );
        meterRegistry.counter(getMetricName("api_requests_total"), tags).increment();
    }
    
    /**
     * Записать время ответа API
     */
    public void recordApiResponseTime(String endpoint, long timeMs) {
        meterRegistry.timer(getMetricName("api_response_time"), 
                          Tags.of(Tag.of("endpoint", endpoint))).record(java.time.Duration.ofMillis(timeMs));
    }
    
    /**
     * Записать количество синхронизаций
     */
    public void recordSync(String status) {
        Tags tags = Tags.of(Tag.of("status", status));
        meterRegistry.counter(getMetricName("sync_total"), tags).increment();
    }
    
    /**
     * Записать время синхронизации
     */
    public void recordSyncTime(long timeMs) {
        meterRegistry.timer(getMetricName("sync_duration")).record(java.time.Duration.ofMillis(timeMs));
    }
    
    /**
     * Получить полное имя метрики с префиксом маркетплейса
     */
    protected String getMetricName(String metricName) {
        return marketplaceName.toLowerCase() + "_" + metricName;
    }
    
    /**
     * Получить название маркетплейса
     */
    public String getMarketplaceName() {
        return marketplaceName;
    }
    
    /**
     * Записать кастомную метрику
     */
    public void recordCustomMetric(String metricName, double value, String... tags) {
        Tags metricTags = Tags.empty();
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                metricTags = metricTags.and(Tag.of(tags[i], tags[i + 1]));
            }
        }
        meterRegistry.gauge(getMetricName(metricName), metricTags, value);
    }
}

