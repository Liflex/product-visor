package ru.dmitartur.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.events.EventType;

@Component
@RequiredArgsConstructor
public class StockEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${kafka.topics.stock-events:stock-events}")
	private String stockEventsTopic;

	public void sendStockChanged(Long productId, String article, int oldQty, int newQty,
			String changeReason, String sourceSystem, String sourceId,
			String originMarket) {
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

		kafkaTemplate.send(stockEventsTopic, article, e.toString());
	}

	public void sendStockChangedForWarehouse(Long productId, String article,
			String warehouseId, String warehouseExternalId, String warehouseType,
			int oldQty, int newQty, String changeReason, String sourceSystem,
			String sourceId, String originMarket) {
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

		kafkaTemplate.send(stockEventsTopic, article, e.toString());
	}
}


