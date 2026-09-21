package com.sl.express.transport.service;

import com.sl.express.common.event.OrderCreatedEvent;
import com.sl.express.transport.dto.TransportStatusUpdateRequest;
import com.sl.express.transport.vo.TransportTrackVO;
import java.util.List;

public interface TransportTaskService {
    void createFromOrderEvent(OrderCreatedEvent event);
    void claim(String orderNo, Long courierId);
    void updateStatus(String orderNo, Long courierId, TransportStatusUpdateRequest request);
    List<TransportTrackVO> listTracks(String orderNo);
}
