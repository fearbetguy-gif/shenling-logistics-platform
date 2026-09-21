package com.sl.express.order.service;

import com.sl.express.order.vo.OrderSummaryVO;
import com.sl.express.order.dto.OrderCreateRequest;

import java.util.List;

public interface OrderQueryService {

    OrderSummaryVO create(Long userId, String requestId, OrderCreateRequest request);

    List<OrderSummaryVO> listByUserId(Long userId);

    OrderSummaryVO getByOrderNo(Long userId, String orderNo);

    OrderSummaryVO cancel(Long userId, String orderNo);

    void updateStatusFromTransport(String orderNo, String status);
}
