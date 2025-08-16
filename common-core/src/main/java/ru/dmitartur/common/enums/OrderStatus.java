package ru.dmitartur.common.enums;

/**
 * Статусы заказов
 */
public enum OrderStatus {
    // Ozon статусы
    AWAITING_PACKAGING("awaiting_packaging", "Ожидает упаковки"),
    AWAITING_DELIVER("awaiting_deliver", "Ожидает доставки"),
    DELIVERING("delivering", "Доставляется"),
    DELIVERED("delivered", "Доставлен"),
    CANCELLED("cancelled", "Отменен"),
    
    // Общие статусы
    COMPLETED("completed", "Завершен"),
    PROCESSING("processing", "Обрабатывается"),
    SHIPPED("shipped", "Отправлен"),
    PENDING("pending", "В ожидании"),
    FAILED("failed", "Ошибка"),
    UNKNOWN("unknown", "Неизвестно");

    private final String code;
    private final String displayName;

    OrderStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получить статус по коду
     */
    public static OrderStatus fromCode(String code) {
        if (code == null) return UNKNOWN;
        
        for (OrderStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
