package com.sl.express.order.enums;

public enum OrderStatus {
    WAIT_PICKUP("待揽收"), PICKED_UP("已揽收"), TRANSPORTING("运输中"),
    DELIVERING("派送中"), SIGNED("已签收"), CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) { this.description = description; }

    public boolean canTransitTo(OrderStatus target) {
        return switch (this) {
            case WAIT_PICKUP -> target == PICKED_UP || target == CANCELLED;
            case PICKED_UP -> target == TRANSPORTING;
            case TRANSPORTING -> target == DELIVERING;
            case DELIVERING -> target == SIGNED;
            case SIGNED, CANCELLED -> false;
        };
    }

    public String getDescription() { return description; }
}
