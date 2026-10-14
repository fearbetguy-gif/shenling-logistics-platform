package com.sl.express.transport.route.impl;

import com.sl.express.common.exception.BusinessException;
import com.sl.express.transport.route.DijkstraRouteService;
import com.sl.express.transport.route.RouteEdge;
import com.sl.express.transport.route.RouteResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

@Service
public class DijkstraRouteServiceImpl implements DijkstraRouteService {

    @Override
    public RouteResult calculate(Map<String, List<RouteEdge>> graph, String start, String end) {
        validateInput(graph, start, end);

        // distance 保存“当前已知的起点到某个节点的最短距离”。
        Map<String, Long> distance = new HashMap<>();
        // previous 保存最短路径中每个节点的前驱节点，用于最后反向还原完整路线。
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<PathState> queue = new PriorityQueue<>();

        distance.put(start, 0L);
        queue.offer(new PathState(start, 0L));

        while (!queue.isEmpty()) {
            PathState current = queue.poll();
            if (!visited.add(current.node())) {
                continue;
            }
            if (current.node().equals(end)) {
                break;
            }

            for (RouteEdge edge : graph.getOrDefault(current.node(), List.of())) {
                if (visited.contains(edge.to())) {
                    continue;
                }
                long candidateDistance = current.distance() + edge.distance();
                long knownDistance = distance.getOrDefault(edge.to(), Long.MAX_VALUE);
                if (candidateDistance < knownDistance) {
                    distance.put(edge.to(), candidateDistance);
                    previous.put(edge.to(), current.node());
                    queue.offer(new PathState(edge.to(), candidateDistance));
                }
            }
        }

        if (!distance.containsKey(end)) {
            throw new BusinessException("物流节点之间不存在可达路径");
        }
        return new RouteResult(buildPath(previous, start, end),
                java.math.BigDecimal.valueOf(distance.get(end), 3));
    }

    private void validateInput(Map<String, List<RouteEdge>> graph, String start, String end) {
        if (graph == null || graph.isEmpty()) {
            throw new BusinessException("物流节点图不能为空");
        }
        if (start == null || start.isBlank() || end == null || end.isBlank()) {
            throw new BusinessException("起点和终点不能为空");
        }
        if (!graph.containsKey(start)) {
            throw new BusinessException("起点节点不存在");
        }
    }

    private List<String> buildPath(Map<String, String> previous, String start, String end) {
        List<String> path = new ArrayList<>();
        String current = end;
        while (current != null) {
            path.add(current);
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private record PathState(String node, long distance) implements Comparable<PathState> {
        @Override
        public int compareTo(PathState other) {
            return Long.compare(distance, other.distance);
        }
    }
}
