package com.sl.express.transport.controller;


import com.sl.express.common.result.Result;
import com.sl.express.transport.service.TransportTaskService;
import com.sl.express.transport.vo.TransportTrackVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/transport")
public class InternalTransportController {

    private final TransportTaskService transportTaskService;

    public InternalTransportController(TransportTaskService transportTaskService) {
        this.transportTaskService = transportTaskService;
    }

    @GetMapping("/{orderNo}/tracks")
    public Result<List<TransportTrackVO>> listTracks(
            @PathVariable String orderNo) {
        return Result.success(transportTaskService.listTracks(orderNo));
    }
}
