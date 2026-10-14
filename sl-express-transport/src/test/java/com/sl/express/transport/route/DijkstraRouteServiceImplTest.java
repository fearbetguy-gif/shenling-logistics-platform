package com.sl.express.transport.route;

import com.sl.express.transport.route.impl.DijkstraRouteServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DijkstraRouteServiceImplTest {

    private final DijkstraRouteService service = new DijkstraRouteServiceImpl();

    @Test
    void shouldReturnShortestPathInsteadOfDirectLongerPath() {
        Map<String, List<RouteEdge>> graph = Map.of(
                "WAREHOUSE", List.of(new RouteEdge("HUB", 10), new RouteEdge("STATION", 40)),
                "HUB", List.of(new RouteEdge("STATION", 15)),
                "STATION", List.of(new RouteEdge("CUSTOMER", 8)),
                "CUSTOMER", List.of()
        );

        RouteResult result = service.calculate(graph, "WAREHOUSE", "CUSTOMER");

        assertEquals(List.of("WAREHOUSE", "HUB", "STATION", "CUSTOMER"), result.nodes());
        assertEquals(new BigDecimal("0.033"), result.distance());
    }

    @Test
    void shouldRejectUnreachableDestination() {
        Map<String, List<RouteEdge>> graph = Map.of(
                "WAREHOUSE", List.of(new RouteEdge("HUB", 10)),
                "HUB", List.of(),
                "CUSTOMER", List.of()
        );

        assertThrows(RuntimeException.class,
                () -> service.calculate(graph, "WAREHOUSE", "CUSTOMER"));
    }
}
