package com.sl.express.order.client;

import feign.Request;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

public class TransportFeignConfig {

    @Bean
    public Request.Options transportRequestOptions() {
        return new Request.Options(2, TimeUnit.SECONDS,
                3, TimeUnit.SECONDS, true);
    }
}
