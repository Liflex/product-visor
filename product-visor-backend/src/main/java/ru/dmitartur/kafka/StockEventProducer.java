package ru.dmitartur.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.events.EventType;
import ru.dmitartur.common.kafka.KafkaTopics;

@Component
@RequiredArgsConstructor
public class StockEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void sendStockChangedForWarehouse(Long productId, String article,
			String warehouseId, String warehouseExternalId, String warehouseType,
			int oldQty, int newQty, String changeReason, String sourceSystem,
			String sourceId, String originMarket, String companyId) {
		ObjectNode e = objectMapper.createObjectNode();
		e.put("eventType", EventType.STOCK_CHANGED.name());
		e.put("productId", productId);
		e.put("article", article);
		e.put("oldQuantity", oldQty);
		e.put("newQuantity", newQty);
		e.put("changeReason", changeReason);
		e.put("sourceSystem", sourceSystem);
		e.put("sourceId", sourceId);
		if (originMarket != null) {
			e.put("originMarket", originMarket);
		}
		if (warehouseId != null) {
			e.put("warehouseId", warehouseId);
		}
		if (warehouseExternalId != null) {
			e.put("warehouseExternalId", warehouseExternalId);
		}
		if (warehouseType != null) {
			e.put("warehouseType", warehouseType);
		}
		e.put("companyId", companyId);

		kafkaTemplate.send(KafkaTopics.STOCK_EVENTS_TOPIC, article, e.toString());
	}
}


