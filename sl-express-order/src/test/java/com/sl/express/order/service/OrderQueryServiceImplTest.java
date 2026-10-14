package com.sl.express.order.service;

import com.sl.express.order.dto.OrderCreateRequest;
import com.sl.express.order.entity.OrderInfo;
import com.sl.express.order.mapper.OrderInfoMapper;
import com.sl.express.order.service.impl.OrderQueryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderQueryServiceImplTest {

    @Test
    void shouldCreateOrderWithInitialWaitingPickupStatus() {
        OrderInfoMapper mapper = mock(OrderInfoMapper.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        RedisScript<Long> script = mock(RedisScript.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class))).thenReturn(1L);
        OrderQueryServiceImpl service = new OrderQueryServiceImpl(mapper, redisTemplate, script, rabbitTemplate,
                new ObjectMapper());

        var result = service.create(7L, "request-001", new OrderCreateRequest(
                "陈默", "13800138000", "杭州西湖区",
                "林晓", "13900139000", "上海浦东新区", "文件"));

        assertThat(result.orderNo()).startsWith("ZD");
        assertThat(result.status()).isEqualTo("WAIT_PICKUP");
        verify(mapper).insert(any(OrderInfo.class));
    }

    @Test
    void shouldCancelOnlyWaitingPickupOrder() {
        OrderInfo order = new OrderInfo();
        order.setId(1L);
        order.setUserId(7L);
        order.setOrderNo("ZD202609210002");
        order.setStatus("WAIT_PICKUP");
        OrderInfoMapper mapper = mock(OrderInfoMapper.class);
        when(mapper.selectOne(any())).thenReturn(order);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        OrderQueryServiceImpl service = new OrderQueryServiceImpl(mapper, redisTemplate,
                mock(RedisScript.class), mock(RabbitTemplate.class), new ObjectMapper());

        var result = service.cancel(7L, order.getOrderNo());

        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(mapper).updateById(order);
        verify(redisTemplate).delete("zd:order:detail:7:ZD202609210002");
    }

    @Test
    void shouldRejectCancelAfterPickup() {
        OrderInfo order = new OrderInfo();
        order.setUserId(7L);
        order.setOrderNo("ZD202609210003");
        order.setStatus("PICKED_UP");
        OrderInfoMapper mapper = mock(OrderInfoMapper.class);
        when(mapper.selectOne(any())).thenReturn(order);
        OrderQueryServiceImpl service = new OrderQueryServiceImpl(mapper, mock(StringRedisTemplate.class),
                mock(RedisScript.class), mock(RabbitTemplate.class), new ObjectMapper());

        assertThrows(RuntimeException.class, () -> service.cancel(7L, order.getOrderNo()));
    }
}
