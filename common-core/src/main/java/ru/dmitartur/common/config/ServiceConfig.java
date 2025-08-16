package ru.dmitartur.common.config;

/**
 * Утилита для получения конфигурации сервисов
 * Использует централизованные порты и URL
 */
public final class ServiceConfig {
    
    /**
     * Получить gRPC URL для product-service
     */
    public static String getProductServiceGrpcUrl() {
        return ServiceUrls.PRODUCT_VISOR_BACKEND_GRPC_URL;
    }
    
    /**
     * Получить gRPC URL для order-service
     */
    public static String getOrderServiceGrpcUrl() {
        return ServiceUrls.ORDER_SERVICE_GRPC_URL;
    }
    
    /**
     * Получить HTTP URL для product-service
     */
    public static String getProductServiceHttpUrl() {
        return ServiceUrls.PRODUCT_VISOR_BACKEND_HTTP_URL;
    }
    
    /**
     * Получить Kafka URL
     */
    public static String getKafkaUrl() {
        return ServiceUrls.KAFKA_URL;
    }
    
    /**
     * Получить PostgreSQL URL
     */
    public static String getPostgresqlUrl() {
        return ServiceUrls.POSTGRESQL_URL;
    }
    
    private ServiceConfig() {
        // Утилитный класс
    }
}
