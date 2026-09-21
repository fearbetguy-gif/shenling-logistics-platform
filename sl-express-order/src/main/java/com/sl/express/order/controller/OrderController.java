package com.sl.express.order.controller;

import com.sl.express.common.result.Result;
import com.sl.express.order.dto.OrderCreateRequest;
import com.sl.express.order.service.OrderQueryService;
import com.sl.express.order.vo.OrderSummaryVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderQueryService orderQueryService;

    public OrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/list")
    public Result<List<OrderSummaryVO>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(orderQueryService.listByUserId(userId));
    }

    @PostMapping
    public Result<OrderSummaryVO> create(@RequestHeader("X-User-Id") Long userId,
                                         @RequestHeader("X-Request-Id") String requestId,
                                         @Valid @RequestBody OrderCreateRequest request) {
        return Result.success(orderQueryService.create(userId, requestId, request));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderSummaryVO> detail(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable @NotBlank String orderNo) {
        return Result.success(orderQueryService.getByOrderNo(userId, orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<OrderSummaryVO> cancel(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable @NotBlank String orderNo) {
        return Result.success(orderQueryService.cancel(userId, orderNo));
    }
}
