package com.sl.express.transport.route.impl;

import com.sl.express.transport.route.DijkstraRouteService;
import com.sl.express.transport.route.RouteEdge;
import com.sl.express.transport.route.RoutePlanningService;
import com.sl.express.transport.route.RouteResult;
import com.sl.express.transport.entity.RouteEdgeEntity;
import com.sl.express.transport.entity.RouteNode;
import com.sl.express.transport.mapper.RouteEdgeMapper;
import com.sl.express.transport.mapper.RouteNodeMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutePlanningServiceImpl implements RoutePlanningService {

    private final DijkstraRouteService dijkstraRouteService;
    private final RouteNodeMapper routeNodeMapper;
    private final RouteEdgeMapper routeEdgeMapper;

    public RoutePlanningServiceImpl(DijkstraRouteService dijkstraRouteService,
                                    RouteNodeMapper routeNodeMapper,
                                    RouteEdgeMapper routeEdgeMapper) {
        this.dijkstraRouteService = dijkstraRouteService;
        this.routeNodeMapper = routeNodeMapper;
        this.routeEdgeMapper = routeEdgeMapper;
    }

    @Override
    public RouteResult findShortestPath(String start, String end) {
        return dijkstraRouteService.calculate(loadGraph(), start, end);
    }

    private Map<String, List<RouteEdge>> loadGraph() {
        Map<String, List<RouteEdge>> graph = new HashMap<>();
        for (RouteNode node : routeNodeMapper.selectList(null)) {
            graph.put(node.getNodeCode(), new java.util.ArrayList<>());
        }
        for (RouteEdgeEntity edge : routeEdgeMapper.selectList(null)) {
            long distanceMeters = edge.getDistanceKm()
                    .multiply(BigDecimal.valueOf(1000))
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();
            graph.computeIfAbsent(edge.getFromNodeCode(), ignored -> new java.util.ArrayList<>())
                    .add(new RouteEdge(edge.getToNodeCode(), distanceMeters));
        }
        return graph;
    }
}
