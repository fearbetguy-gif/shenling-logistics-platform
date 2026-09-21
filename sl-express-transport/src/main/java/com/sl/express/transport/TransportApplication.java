package com.sl.express.transport;

import com.sl.express.common.handler.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import(GlobalExceptionHandler.class)
@MapperScan("com.sl.express.transport.mapper")
@EnableFeignClients
@EnableScheduling
@EnableDiscoveryClient
public class TransportApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransportApplication.class, args);
    }
}
