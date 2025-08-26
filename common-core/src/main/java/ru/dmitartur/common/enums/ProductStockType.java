package ru.dmitartur.common.enums;

/**
 * Типы остатков товаров
 */
public enum ProductStockType {
    FBS("FBS", "Склад продавца"),
    YANDEX_FBO("YANDEX_FBO", "Склад Yandex"),
    OZON_FBO("OZON_FBO", "Склад Ozon");

    private final String code;
    private final String description;

    ProductStockType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Получить тип по коду
     */
    public static ProductStockType fromCode(String code) {
        for (ProductStockType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStockType code: " + code);
    }

    /**
     * Проверить, является ли тип FBS
     */
    public boolean isFbs() {
        return this == FBS;
    }

    /**
     * Проверить, является ли тип FBO
     */
    public boolean isFbo() {
        return this == YANDEX_FBO || this == OZON_FBO;
    }

    /**
     * Получить маркетплейс для FBO типа
     */
    public String getMarketplace() {
        switch (this) {
            case YANDEX_FBO:
                return "YANDEX";
            case OZON_FBO:
                return "OZON";
            default:
                return null;
        }
    }
}

