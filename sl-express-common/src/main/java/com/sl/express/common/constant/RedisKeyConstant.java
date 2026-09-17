package com.sl.express.common.constant;

/** Redis Key 集中管理，避免各服务手写 Key 导致命名不一致。 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
    }

    public static final String ORDER_DETAIL = "sl:order:detail:";
    public static final String ORDER_CREATE_IDEMPOTENT = "sl:order:create:idempotent:";
}
