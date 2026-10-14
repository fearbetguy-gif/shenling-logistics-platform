package com.sl.express.order.client;

import com.sl.express.common.result.Result;
import com.sl.express.order.vo.AssistantTrackVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "sl-express-transport",
        url = "${TRANSPORT_SERVICE_URL:http://127.0.0.1:8083}",
        configuration = TransportFeignConfig.class)
public interface TransportServiceClient {

    @GetMapping("/internal/transport/{orderNo}/tracks")
    Result<List<AssistantTrackVO>> listTracks(
            @PathVariable("orderNo") String orderNo);
}
