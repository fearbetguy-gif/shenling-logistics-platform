package com.sl.express.transport.consumer;

import com.sl.express.common.event.OrderCreatedEvent;
import com.sl.express.transport.config.RabbitMqConfig;
import com.sl.express.transport.service.TransportTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final TransportTaskService transportTaskService;

    public OrderCreatedConsumer(TransportTaskService transportTaskService) {
        this.transportTaskService = transportTaskService;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_CREATED_QUEUE)
    public void consume(OrderCreatedEvent event) {
        log.info("收到订单创建事件：orderNo={}", event.orderNo());
        transportTaskService.createFromOrderEvent(event);
    }
}
