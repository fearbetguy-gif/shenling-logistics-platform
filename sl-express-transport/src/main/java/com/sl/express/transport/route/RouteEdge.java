package com.sl.express.transport.route;

/**
 * 表示一条物流节点之间的连线。
 *
 * @param to       目标节点编码
 * @param distance 当前连线的距离，单位：米
 */
public record RouteEdge(String to, long distance) {
    public RouteEdge {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("目标节点不能为空");
        }
        if (distance <= 0) {
            throw new IllegalArgumentException("节点距离必须大于0");
        }
    }
}
