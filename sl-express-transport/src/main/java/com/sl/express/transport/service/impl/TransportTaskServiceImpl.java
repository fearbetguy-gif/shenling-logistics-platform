package com.sl.express.transport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sl.express.common.event.OrderCreatedEvent;
import com.sl.express.common.exception.BusinessException;
import com.sl.express.transport.dto.TransportStatusUpdateRequest;
import com.sl.express.transport.client.OrderServiceClient;
import com.sl.express.transport.entity.MqConsumeRecord;
import com.sl.express.transport.entity.TransportTask;
import com.sl.express.transport.entity.TransportTrack;
import com.sl.express.transport.entity.OrderStatusSyncRetry;
import com.sl.express.transport.enums.TransportStatus;
import com.sl.express.transport.mapper.MqConsumeRecordMapper;
import com.sl.express.transport.mapper.TransportTaskMapper;
import com.sl.express.transport.mapper.TransportTrackMapper;
import com.sl.express.transport.mapper.OrderStatusSyncRetryMapper;
import com.sl.express.transport.service.TransportTaskService;
import com.sl.express.transport.vo.TransportTrackVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TransportTaskServiceImpl implements TransportTaskService {
    private static final Logger log = LoggerFactory.getLogger(TransportTaskServiceImpl.class);
    private static final String MESSAGE_TYPE = "ORDER_CREATED";

    private final TransportTaskMapper transportTaskMapper;
    private final MqConsumeRecordMapper consumeRecordMapper;
    private final TransportTrackMapper transportTrackMapper;
    private final OrderServiceClient orderServiceClient;
    private final OrderStatusSyncRetryMapper syncRetryMapper;

    public TransportTaskServiceImpl(TransportTaskMapper transportTaskMapper,
                                    MqConsumeRecordMapper consumeRecordMapper,
                                    TransportTrackMapper transportTrackMapper,
                                    OrderServiceClient orderServiceClient,
                                    OrderStatusSyncRetryMapper syncRetryMapper) {
        this.transportTaskMapper = transportTaskMapper;
        this.consumeRecordMapper = consumeRecordMapper;
        this.transportTrackMapper = transportTrackMapper;
        this.orderServiceClient = orderServiceClient;
        this.syncRetryMapper = syncRetryMapper;
    }

    @Override
    @Transactional
    public void createFromOrderEvent(OrderCreatedEvent event) {
        MqConsumeRecord record = new MqConsumeRecord();
        record.setMessageKey(event.orderNo());
        record.setMessageType(MESSAGE_TYPE);
        record.setConsumedAt(LocalDateTime.now());
        try {
            consumeRecordMapper.insert(record);
        } catch (DuplicateKeyException duplicate) {
            log.info("订单创建消息已消费，忽略重复消息：orderNo={}", event.orderNo());
            return;
        }

        TransportTask task = new TransportTask();
        task.setOrderNo(event.orderNo());
        task.setUserId(event.userId());
        task.setSenderAddress(event.senderAddress());
        task.setReceiverAddress(event.receiverAddress());
        task.setStatus("WAIT_ASSIGN");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(task.getCreatedAt());
        transportTaskMapper.insert(task);
        log.info("运输任务创建成功：orderNo={}, taskId={}", event.orderNo(), task.getId());
    }

    @Override
    @Transactional
    public void claim(String orderNo, Long courierId) {
        TransportTask task = getTask(orderNo);
        if (!TransportStatus.WAIT_ASSIGN.name().equals(task.getStatus())) {
            throw new BusinessException("当前任务不可领取");
        }
        task.setCourierId(courierId);
        task.setStatus(TransportStatus.WAIT_PICKUP.name());
        task.setUpdatedAt(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        addTrack(orderNo, TransportStatus.WAIT_PICKUP, "快递员已领取运输任务", courierId);
    }

    @Override
    @Transactional
    public void updateStatus(String orderNo, Long courierId, TransportStatusUpdateRequest request) {
        TransportTask task = getTask(orderNo);
        if (!courierId.equals(task.getCourierId())) {
            throw new BusinessException("只有任务快递员可以更新状态");
        }
        TransportStatus current = parseStatus(task.getStatus());
        TransportStatus target = parseStatus(request.status());
        if (!current.canTransitTo(target)) {
            throw new BusinessException("非法状态流转：" + current + " -> " + target);
        }
        task.setStatus(target.name());
        task.setUpdatedAt(LocalDateTime.now());
        transportTaskMapper.updateById(task);
        addTrack(orderNo, target, request.description(), courierId);
        syncOrderStatus(orderNo, target);
    }

    @Override
    public List<TransportTrackVO> listTracks(String orderNo) {
        return transportTrackMapper.selectList(new LambdaQueryWrapper<TransportTrack>()
                        .eq(TransportTrack::getOrderNo, orderNo)
                        .orderByDesc(TransportTrack::getCreatedAt))
                .stream().map(track -> new TransportTrackVO(track.getStatus(),
                        track.getDescription(), track.getCreatedAt())).toList();
    }

    private TransportTask getTask(String orderNo) {
        TransportTask task = transportTaskMapper.selectOne(new LambdaQueryWrapper<TransportTask>()
                .eq(TransportTask::getOrderNo, orderNo));
        if (task == null) throw new BusinessException("运输任务不存在");
        return task;
    }

    private TransportStatus parseStatus(String status) {
        try {
            return TransportStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("未知运输状态：" + status);
        }
    }

    private void addTrack(String orderNo, TransportStatus status, String description, Long operatorId) {
        TransportTrack track = new TransportTrack();
        track.setOrderNo(orderNo);
        track.setStatus(status.name());
        track.setDescription(description);
        track.setOperatorId(operatorId);
        track.setCreatedAt(LocalDateTime.now());
        transportTrackMapper.insert(track);
    }

    private void syncOrderStatus(String orderNo, TransportStatus status) {
        try {
            orderServiceClient.updateStatus(orderNo, Map.of("status", status.name()));
        } catch (Exception exception) {
            recordSyncFailure(orderNo, status, exception.getMessage());
            log.warn("订单状态同步失败，后续需要补偿：orderNo={}, status={}", orderNo, status, exception);
        }
    }

    private void recordSyncFailure(String orderNo, TransportStatus status, String error) {
        OrderStatusSyncRetry retry = syncRetryMapper.selectOne(new LambdaQueryWrapper<OrderStatusSyncRetry>()
                .eq(OrderStatusSyncRetry::getOrderNo, orderNo)
                .eq(OrderStatusSyncRetry::getTransportStatus, status.name()));
        if (retry == null) {
            retry = new OrderStatusSyncRetry();
            retry.setOrderNo(orderNo);
            retry.setTransportStatus(status.name());
            retry.setRetryCount(0);
            retry.setCreatedAt(LocalDateTime.now());
        }
        retry.setNextRetryAt(LocalDateTime.now().plusSeconds(30));
        retry.setLastError(error == null ? "unknown error" : error.substring(0, Math.min(error.length(), 500)));
        retry.setUpdatedAt(LocalDateTime.now());
        if (retry.getId() == null) syncRetryMapper.insert(retry);
        else syncRetryMapper.updateById(retry);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryOrderStatusSync() {
        List<OrderStatusSyncRetry> pending = syncRetryMapper.selectList(new LambdaQueryWrapper<OrderStatusSyncRetry>()
                .le(OrderStatusSyncRetry::getNextRetryAt, LocalDateTime.now())
                .lt(OrderStatusSyncRetry::getRetryCount, 10)
                .orderByAsc(OrderStatusSyncRetry::getNextRetryAt)
                .last("limit 20"));
        for (OrderStatusSyncRetry retry : pending) {
            try {
                orderServiceClient.updateStatus(retry.getOrderNo(), Map.of("status", retry.getTransportStatus()));
                syncRetryMapper.deleteById(retry.getId());
                log.info("订单状态补偿成功：orderNo={}, status={}", retry.getOrderNo(), retry.getTransportStatus());
            } catch (Exception exception) {
                retry.setRetryCount(retry.getRetryCount() + 1);
                retry.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(300, 30L * retry.getRetryCount())));
                retry.setLastError(exception.getMessage());
                syncRetryMapper.updateById(retry);
            }
        }
    }
}
