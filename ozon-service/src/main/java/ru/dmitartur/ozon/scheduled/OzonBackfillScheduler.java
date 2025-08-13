package ru.dmitartur.ozon.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dmitartur.ozon.dto.DateRangeDto;
import ru.dmitartur.ozon.metrics.OzonMetrics;
import ru.dmitartur.ozon.service.OzonOrderSyncService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ozon-sync", name = "enabled", havingValue = "true")
public class OzonBackfillScheduler {

    private final OzonOrderSyncService syncService;
    private final OzonMetrics metrics;

    @Scheduled(fixedDelayString = "${ozon-sync.fixed-delay-ms:120000}")
    public void run() {
        try {
            var now = java.time.OffsetDateTime.now();
            var from = now.minusMinutes(Integer.getInteger("ozon-sync.window-minutes", 15)).toString();
            var to = now.toString();
            DateRangeDto range = new DateRangeDto();
            range.setFrom(from);
            range.setTo(to);
            int pageSize = Integer.getInteger("ozon-sync.page-size", 100);
            int upserted = syncService.backfillFboOrders(range, pageSize);
            metrics.recordUpserted(upserted);
            metrics.recordBatch("ok");
        } catch (Exception e) {
            log.warn("Ozon backfill failed", e);
            metrics.recordError("run");
        }
    }
}


