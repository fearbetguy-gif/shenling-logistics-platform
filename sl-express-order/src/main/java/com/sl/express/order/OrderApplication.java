package com.sl.express.order;

import com.sl.express.common.handler.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@Import(GlobalExceptionHandler.class)
@MapperScan("com.sl.express.order.mapper")
@EnableDiscoveryClient
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
