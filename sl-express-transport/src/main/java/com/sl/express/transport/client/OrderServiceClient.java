package com.sl.express.transport.client;

import com.sl.express.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "sl-express-order", url = "${ORDER_SERVICE_URL:http://127.0.0.1:8082}",
        configuration = OrderServiceFeignConfig.class)
public interface OrderServiceClient {
    @PostMapping("/internal/orders/{orderNo}/status")
    Result<Void> updateStatus(@PathVariable String orderNo, @RequestBody Map<String, String> request);
}
