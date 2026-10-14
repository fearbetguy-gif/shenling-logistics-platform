package com.sl.express.transport.route;

import com.sl.express.transport.route.impl.DijkstraRouteServiceImpl;
import com.sl.express.transport.route.impl.RoutePlanningServiceImpl;
import com.sl.express.transport.mapper.RouteEdgeMapper;
import com.sl.express.transport.mapper.RouteNodeMapper;
import com.sl.express.transport.entity.RouteEdgeEntity;
import com.sl.express.transport.entity.RouteNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoutePlanningServiceImplTest {

    @Test
    void shouldCalculateDemoLogisticsRoute() {
        RouteNodeMapper nodeMapper = mock(RouteNodeMapper.class);
        RouteEdgeMapper edgeMapper = mock(RouteEdgeMapper.class);
        when(nodeMapper.selectList(null)).thenReturn(List.of(
                node("WAREHOUSE"), node("BEIJING_HUB"), node("LANGFANG_HUB"),
                node("LANGFANG_STATION"), node("CUSTOMER")));
        when(edgeMapper.selectList(null)).thenReturn(List.of(
                edge("WAREHOUSE", "BEIJING_HUB", "10.00"),
                edge("WAREHOUSE", "LANGFANG_HUB", "45.00"),
                edge("BEIJING_HUB", "LANGFANG_HUB", "15.00"),
                edge("LANGFANG_HUB", "LANGFANG_STATION", "8.00"),
                edge("LANGFANG_STATION", "CUSTOMER", "5.00")));
        RoutePlanningService service = new RoutePlanningServiceImpl(
                new DijkstraRouteServiceImpl(), nodeMapper, edgeMapper);

        RouteResult result = service.findShortestPath("WAREHOUSE", "CUSTOMER");

        assertEquals(List.of("WAREHOUSE", "BEIJING_HUB", "LANGFANG_HUB",
                "LANGFANG_STATION", "CUSTOMER"), result.nodes());
        assertEquals(new BigDecimal("38.000"), result.distance());
    }

    private RouteNode node(String code) {
        RouteNode node = new RouteNode();
        node.setNodeCode(code);
        return node;
    }

    private RouteEdgeEntity edge(String from, String to, String distance) {
        RouteEdgeEntity edge = new RouteEdgeEntity();
        edge.setFromNodeCode(from);
        edge.setToNodeCode(to);
        edge.setDistanceKm(new BigDecimal(distance));
        return edge;
    }
}
