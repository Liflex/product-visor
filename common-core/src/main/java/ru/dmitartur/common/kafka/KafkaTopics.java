package ru.dmitartur.common.kafka;

/**
 * Centralized Kafka topics registry.
 * Prefer using enum {@link KafkaTopic} for new code to get type-safety and discoverability.
 */
public final class KafkaTopics {
    // Centralized literal constants for annotation usage (must be compile-time constants)
    public static final String ORDER_EVENTS_TOPIC = "order-events";           // Order domain events
    public static final String PRODUCT_EVENTS_TOPIC = "product-events";       // Product domain events
    public static final String USER_EVENTS_TOPIC = "user.events";             // User domain events
    public static final String STOCK_EVENTS_TOPIC = "stock-events";           // Stock change events

    public static final String STOCK_SYNC_TOPIC = "stock-sync";               // Stock sync requests
    public static final String STOCK_SYNC_RESPONSE_TOPIC = "stock-sync-response"; // Stock sync responses

    public static final String ORDER_SYNC_TOPIC = "order-sync";               // Order sync/upsert requests
    public static final String ORDER_SYNC_RESPONSE_TOPIC = "order-sync-response"; // Order sync/upsert responses

    private KafkaTopics() {}
}
