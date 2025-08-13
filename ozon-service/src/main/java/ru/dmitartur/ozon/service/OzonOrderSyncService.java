package ru.dmitartur.ozon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.dmitartur.common.grpc.OrderInternalServiceGrpc;
import ru.dmitartur.common.grpc.JsonRequest;
import ru.dmitartur.ozon.dto.DateRangeDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonOrderSyncService {
    private final OzonService ozonService;
    private final OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderStub;
    private final ObjectMapper mapper = new ObjectMapper();

    public int backfillFboOrders(DateRangeDto range, int pageSize) {
        int totalUpserted = 0;
        int offset = 0;
        while (true) {
            var req = mapper.createObjectNode();
            req.putObject("createdAt").put("from", range.getFrom()).put("to", range.getTo());
            req.put("limit", pageSize);
            req.put("offset", offset);
            JsonNode page = ozonService.fboPostingList(req);
            JsonRequest grpcReq = JsonRequest.newBuilder().setJson(page.toString()).build();
            var grpcResp = orderStub.upsertOrders(grpcReq);
            try { totalUpserted += mapper.readTree(grpcResp.getJson()).path("upserted").asInt(0); } catch (Exception ignored) {}
            int size = page.path("postings").isArray() ? page.path("postings").size() : 0;
            if (size < pageSize) break;
            offset += pageSize;
        }
        log.info("FBO backfill finished, upserted {} orders", totalUpserted);
        return totalUpserted;
    }
}


