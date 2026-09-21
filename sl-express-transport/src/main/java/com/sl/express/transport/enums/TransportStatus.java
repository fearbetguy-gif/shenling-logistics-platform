package com.sl.express.transport.enums;

/** 运输任务状态，只允许按业务流程向后流转。 */
public enum TransportStatus {
    WAIT_ASSIGN, WAIT_PICKUP, PICKING_UP, IN_TRANSIT, DELIVERING, SIGNED;

    public boolean canTransitTo(TransportStatus target) {
        return switch (this) {
            case WAIT_ASSIGN -> target == WAIT_PICKUP;
            case WAIT_PICKUP -> target == PICKING_UP;
            case PICKING_UP -> target == IN_TRANSIT;
            case IN_TRANSIT -> target == DELIVERING;
            case DELIVERING -> target == SIGNED;
            case SIGNED -> false;
        };
    }
}
