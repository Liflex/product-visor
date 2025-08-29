package ru.dmitartur.ozon.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.events.EventType;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.common.security.CompanyContextHolder;
import ru.dmitartur.ozon.service.OzonService;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockChangeConsumer {

	private final ObjectMapper objectMapper;
	private final OzonService ozonService;

	@Value("${application.market.name:ozon}")
	private String marketName;

	@KafkaListener(topics = KafkaTopics.STOCK_EVENTS_TOPIC, groupId = "${kafka.consumer.group-id:ozon-service-group}")
	public void handleStockChange(String message) throws Exception {
		JsonNode e = objectMapper.readTree(message);
		CompanyContextHolder.setContext(e.path("companyId").asText(), null);
		try {
			if (!EventType.STOCK_CHANGED.name().equals(e.path("eventType").asText())) {
				return;
			}

			String originMarket = e.path("originMarket").asText("");
			if (originMarket.equalsIgnoreCase(marketName)) {
				log.debug("Ignoring own market stock event: originMarket={}", originMarket);
				return;
			}

			String offerId = e.path("article").asText();
			int newQty = e.path("newQuantity").asInt();
			String warehouseExternalId = e.path("warehouseExternalId").asText("");
			String eventMarket = e.path("originMarket").asText("").toLowerCase();
			if (!eventMarket.isEmpty() && !eventMarket.equalsIgnoreCase(marketName)) {
				log.debug("Ignoring stock event for different market: eventMarket={}, serviceMarket={}", eventMarket, marketName);
				return;
			}

			log.info("Processing stock change event for OZON: offerId={}, newQty={}, warehouseId={} (originMarket={})", offerId, newQty, warehouseExternalId, originMarket);
			ozonService.updateStock(offerId, newQty, warehouseExternalId);
		} finally {
			CompanyContextHolder.clear();
		}


	}
}


