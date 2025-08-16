package ru.dmitartur.common.config;

/**
 * Конфигурация портов для всех сервисов
 * Централизованное управление портами
 */
public final class ServicePorts {
    
    // HTTP порты
    public static final int PRODUCT_VISOR_BACKEND_HTTP = 8085;
    public static final int ORDER_SERVICE_HTTP = 9088;
    public static final int OZON_SERVICE_HTTP = 9097;
    
    // gRPC порты
    public static final int PRODUCT_VISOR_BACKEND_GRPC = 9093;
    public static final int ORDER_SERVICE_GRPC = 9099;
    
    // База данных
    public static final int POSTGRESQL_PORT = 5433;
    
    // Kafka
    public static final int KAFKA_PORT = 9092;
    
    // Инфраструктурные сервисы
    public static final int PROMETHEUS_PORT = 9090;
    public static final int GRAFANA_PORT = 3000;
    public static final int REDIS_PORT = 6379;
    public static final int TEMPO_PORT = 3200;
    
    private ServicePorts() {
        // Утилитный класс
    }
}
