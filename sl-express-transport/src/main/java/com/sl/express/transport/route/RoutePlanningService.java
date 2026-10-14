package com.sl.express.transport.route;

public interface RoutePlanningService {
    RouteResult findShortestPath(String start, String end);
}
