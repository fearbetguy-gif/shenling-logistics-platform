package com.sl.express.transport.client;

import feign.Request;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.TimeUnit;

public class OrderServiceFeignConfig {
    @Bean
    public Request.Options orderServiceRequestOptions() {
        return new Request.Options(2, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
    }
}
