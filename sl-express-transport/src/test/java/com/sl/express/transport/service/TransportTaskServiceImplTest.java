package com.sl.express.transport.service;

import com.sl.express.common.event.OrderCreatedEvent;
import com.sl.express.transport.entity.MqConsumeRecord;
import com.sl.express.transport.entity.TransportTask;
import com.sl.express.transport.mapper.MqConsumeRecordMapper;
import com.sl.express.transport.mapper.TransportTaskMapper;
import com.sl.express.transport.mapper.TransportTrackMapper;
import com.sl.express.transport.client.OrderServiceClient;
import com.sl.express.transport.mapper.OrderStatusSyncRetryMapper;
import com.sl.express.transport.service.impl.TransportTaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransportTaskServiceImplTest {

    @Test
    void shouldCreateConsumeRecordBeforeTransportTask() {
        MqConsumeRecordMapper recordMapper = mock(MqConsumeRecordMapper.class);
        TransportTaskMapper taskMapper = mock(TransportTaskMapper.class);
        TransportTrackMapper trackMapper = mock(TransportTrackMapper.class);
        OrderServiceClient orderClient = mock(OrderServiceClient.class);
        OrderStatusSyncRetryMapper retryMapper = mock(OrderStatusSyncRetryMapper.class);
        TransportTaskService service = new TransportTaskServiceImpl(taskMapper, recordMapper, trackMapper, orderClient, retryMapper);
        OrderCreatedEvent event = new OrderCreatedEvent("ZD202609210001", 1L,
                "杭州", "上海", LocalDateTime.now());

        service.createFromOrderEvent(event);

        InOrder inOrder = inOrder(recordMapper, taskMapper);
        inOrder.verify(recordMapper).insert(any(MqConsumeRecord.class));
        inOrder.verify(taskMapper).insert(any(TransportTask.class));
    }

    @Test
    void shouldIgnoreDuplicateMessage() {
        MqConsumeRecordMapper recordMapper = mock(MqConsumeRecordMapper.class);
        TransportTaskMapper taskMapper = mock(TransportTaskMapper.class);
        TransportTrackMapper trackMapper = mock(TransportTrackMapper.class);
        doThrow(new org.springframework.dao.DuplicateKeyException("duplicate"))
                .when(recordMapper).insert(any(MqConsumeRecord.class));
        OrderServiceClient orderClient = mock(OrderServiceClient.class);
        OrderStatusSyncRetryMapper retryMapper = mock(OrderStatusSyncRetryMapper.class);
        TransportTaskService service = new TransportTaskServiceImpl(taskMapper, recordMapper, trackMapper, orderClient, retryMapper);
        OrderCreatedEvent event = new OrderCreatedEvent("ZD202609210001", 1L,
                "杭州", "上海", LocalDateTime.now());

        assertDoesNotThrow(() -> service.createFromOrderEvent(event));
        verify(taskMapper, never()).insert(any(TransportTask.class));
    }
}
