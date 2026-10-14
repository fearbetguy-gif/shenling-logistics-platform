package com.sl.express.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sl.express.common.exception.BusinessException;
import com.sl.express.order.entity.OrderInfo;
import com.sl.express.order.dto.OrderCreateRequest;
import com.sl.express.order.enums.OrderStatus;
import com.sl.express.order.mapper.OrderInfoMapper;
import com.sl.express.order.service.OrderQueryService;
import com.sl.express.order.vo.OrderSummaryVO;
import com.sl.express.common.event.OrderCreatedEvent;
import com.sl.express.order.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private static final Logger log = LoggerFactory.getLogger(OrderQueryServiceImpl.class);
    private static final String ORDER_CACHE_KEY_PREFIX = "zd:order:detail:";

    private final OrderInfoMapper orderInfoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<Long> orderCreateIdempotentScript;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public OrderQueryServiceImpl(OrderInfoMapper orderInfoMapper,
                                 StringRedisTemplate stringRedisTemplate,
                                 RedisScript<Long> orderCreateIdempotentScript,
                                 RabbitTemplate rabbitTemplate,
                                 ObjectMapper objectMapper) {
        this.orderInfoMapper = orderInfoMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderCreateIdempotentScript = orderCreateIdempotentScript;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<OrderSummaryVO> listByUserId(Long userId) {
        return orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getUserId, userId)
                        .orderByDesc(OrderInfo::getCreatedAt))
                .stream().map(this::toVO).toList();
    }

    @Override
    public OrderSummaryVO create(Long userId, String requestId, OrderCreateRequest request) {
        String idempotentKey = "zd:order:create:idempotent:" + userId + ":" + requestId;
        Long acquired = stringRedisTemplate.execute(orderCreateIdempotentScript,
                java.util.List.of(idempotentKey), requestId, "30");
        if (!Long.valueOf(1L).equals(acquired)) {
            throw new BusinessException("订单正在处理中，请勿重复提交");
        }
        LocalDateTime now = LocalDateTime.now();
        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo(now));
        order.setUserId(userId);
        order.setSenderName(request.senderName());
        order.setSenderPhone(request.senderPhone());
        order.setSenderAddress(request.senderAddress());
        order.setReceiverName(request.receiverName());
        order.setReceiverPhone(request.receiverPhone());
        order.setReceiverAddress(request.receiverAddress());
        order.setGoodsType(request.goodsType());
        order.setFreightAmount(java.math.BigDecimal.ZERO);
        order.setStatus(OrderStatus.WAIT_PICKUP.name());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        orderInfoMapper.insert(order);
        rabbitTemplate.convertAndSend(RabbitMqConfig.ORDER_EXCHANGE,
                RabbitMqConfig.ORDER_CREATED_ROUTING_KEY,
                new OrderCreatedEvent(order.getOrderNo(), userId, order.getSenderAddress(),
                        order.getReceiverAddress(), now));
        return toVO(order);
    }

    @Override
    public OrderSummaryVO getByOrderNo(Long userId, String orderNo) {
        String cacheKey = ORDER_CACHE_KEY_PREFIX + userId + ":" + orderNo;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, OrderSummaryVO.class);
            } catch (Exception exception) {
                log.warn("订单缓存反序列化失败，回源数据库：orderNo={}", orderNo, exception);
                stringRedisTemplate.delete(cacheKey);
            }
        }
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        OrderSummaryVO result = toVO(order);
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(result),
                    java.time.Duration.ofMinutes(10));
        } catch (Exception exception) {
            log.warn("订单详情写入缓存失败，不影响主流程：orderNo={}", orderNo, exception);
        }
        return result;
    }

    @Override
    public OrderSummaryVO cancel(Long userId, String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        if (!current.canTransitTo(OrderStatus.CANCELLED)) {
            throw new BusinessException("当前订单状态不可取消：" + current);
        }
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        stringRedisTemplate.delete(ORDER_CACHE_KEY_PREFIX + userId + ":" + orderNo);
        return toVO(order);
    }

    @Override
    public void updateStatusFromTransport(String orderNo, String status) {
        OrderInfo order = orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) throw new BusinessException("订单不存在");
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        OrderStatus target = mapTransportStatus(status);
        if (current == target) return;
        if (!current.canTransitTo(target)) {
            throw new BusinessException("订单状态不允许流转：" + current + " -> " + target);
        }
        order.setStatus(target.name());
        order.setUpdatedAt(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        stringRedisTemplate.delete(ORDER_CACHE_KEY_PREFIX + order.getUserId() + ":" + orderNo);
    }

    private OrderStatus mapTransportStatus(String status) {
        return switch (status) {
            case "PICKING_UP" -> OrderStatus.PICKED_UP;
            case "IN_TRANSIT" -> OrderStatus.TRANSPORTING;
            case "DELIVERING" -> OrderStatus.DELIVERING;
            case "SIGNED" -> OrderStatus.SIGNED;
            default -> throw new BusinessException("不支持同步的运输状态：" + status);
        };
    }

    private OrderSummaryVO toVO(OrderInfo order) {
        return new OrderSummaryVO(order.getOrderNo(), order.getSenderName(), order.getReceiverName(),
                order.getSenderAddress(), order.getReceiverAddress(), order.getGoodsType(),
                order.getFreightAmount(), order.getStatus(), order.getCreatedAt());
    }

    private String generateOrderNo(LocalDateTime now) {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "ZD" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + suffix;
    }
}
