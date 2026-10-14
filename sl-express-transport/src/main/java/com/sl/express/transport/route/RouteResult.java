package com.sl.express.transport.route;

import java.util.List;
import java.math.BigDecimal;

/**
 * 最短路径计算结果。
 *
 * @param nodes    从起点到终点经过的节点，按运输顺序排列
 * @param distance 总距离，单位：公里
 */
public record RouteResult(List<String> nodes, BigDecimal distance) {
    public RouteResult {
        nodes = List.copyOf(nodes);
    }
}
