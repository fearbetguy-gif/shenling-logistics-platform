package com.sl.express.transport.route;

import java.util.List;
import java.util.Map;

public interface DijkstraRouteService {

    /**
     * 根据物流节点图计算起点到终点的最短路径。
     *
     * @param graph 物流节点图，key 是当前节点，value 是可到达的下一个节点
     * @param start 起点节点编码
     * @param end   终点节点编码
     */
    RouteResult calculate(Map<String, List<RouteEdge>> graph, String start, String end);
}
