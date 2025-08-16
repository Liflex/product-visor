package ru.dmitartur.common.config;

/**
 * Конфигурация URL для всех сервисов
 * Централизованное управление URL
 */
public final class ServiceUrls {
    
    // Базовые URL
    public static final String LOCALHOST = "localhost";
    
    // HTTP URL
    public static final String PRODUCT_VISOR_BACKEND_HTTP_URL = 
        "http://" + LOCALHOST + ":" + ServicePorts.PRODUCT_VISOR_BACKEND_HTTP;
    
    public static final String ORDER_SERVICE_HTTP_URL = 
        "http://" + LOCALHOST + ":" + ServicePorts.ORDER_SERVICE_HTTP;
    
    public static final String OZON_SERVICE_HTTP_URL = 
        "http://" + LOCALHOST + ":" + ServicePorts.OZON_SERVICE_HTTP;
    
    // gRPC URL
    public static final String PRODUCT_VISOR_BACKEND_GRPC_URL = 
        LOCALHOST + ":" + ServicePorts.PRODUCT_VISOR_BACKEND_GRPC;
    
    public static final String ORDER_SERVICE_GRPC_URL = 
        LOCALHOST + ":" + ServicePorts.ORDER_SERVICE_GRPC;
    
    // База данных
    public static final String POSTGRESQL_URL = 
        "jdbc:postgresql://" + LOCALHOST + ":" + ServicePorts.POSTGRESQL_PORT + "/product_visor";
    
    // Kafka
    public static final String KAFKA_URL = 
        LOCALHOST + ":" + ServicePorts.KAFKA_PORT;
    
    private ServiceUrls() {
        // Утилитный класс
    }
}
