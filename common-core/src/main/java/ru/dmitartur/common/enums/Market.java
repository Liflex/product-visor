package ru.dmitartur.common.enums;

public enum Market {
    OZON("Ozon"),
    WILDBERRIES("Wildberries"),
    YANDEX_MARKET("Yandex Market"),
    ALIEXPRESS("AliExpress"),
    OTHER("Other");

    private final String displayName;

    Market(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
