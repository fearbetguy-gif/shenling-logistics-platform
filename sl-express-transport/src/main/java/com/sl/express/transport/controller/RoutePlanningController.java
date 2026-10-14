package com.sl.express.transport.controller;

import com.sl.express.common.result.Result;
import com.sl.express.transport.route.RoutePlanningService;
import com.sl.express.transport.route.RouteResult;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/transport/routes")
public class RoutePlanningController {

    private final RoutePlanningService routePlanningService;

    public RoutePlanningController(RoutePlanningService routePlanningService) {
        this.routePlanningService = routePlanningService;
    }

    @GetMapping("/shortest")
    public Result<RouteResult> shortestPath(
            @RequestParam @NotBlank String start,
            @RequestParam @NotBlank String end) {
        return Result.success(routePlanningService.findShortestPath(start, end));
    }
}
