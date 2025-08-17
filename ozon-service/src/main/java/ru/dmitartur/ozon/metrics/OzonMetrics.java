package ru.dmitartur.ozon.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

@Component
public class OzonMetrics {

    private final MeterRegistry meterRegistry;

    public OzonMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordUpserted(int count) {
        if (count <= 0) return;
        meterRegistry.counter("ozon_backfill_upserted_total").increment(count);
    }

    public void recordBatch(String result) {
        Tags tags = Tags.of(Tag.of("result", result == null ? "ok" : result));
        meterRegistry.counter("ozon_backfill_batches_total", tags).increment();
    }

    public void recordError(String stage) {
        Tags tags = Tags.of(Tag.of("stage", stage == null ? "unknown" : stage));
        meterRegistry.counter("ozon_backfill_errors_total", tags).increment();
    }
}






