package com.sl.express.transport.controller;

import com.sl.express.common.result.Result;
import com.sl.express.transport.dto.TransportStatusUpdateRequest;
import com.sl.express.transport.service.TransportTaskService;
import com.sl.express.transport.vo.TransportTrackVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/transport/tasks")
public class TransportTaskController {
    private final TransportTaskService transportTaskService;
    public TransportTaskController(TransportTaskService transportTaskService) {
        this.transportTaskService = transportTaskService;
    }
    @PostMapping("/{orderNo}/claim")
    public Result<Void> claim(@PathVariable String orderNo,
                              @RequestHeader("X-User-Id") Long courierId) {
        transportTaskService.claim(orderNo, courierId);
        return Result.success();
    }
    @PostMapping("/{orderNo}/status")
    public Result<Void> updateStatus(@PathVariable String orderNo,
                                     @RequestHeader("X-User-Id") Long courierId,
                                     @Valid @RequestBody TransportStatusUpdateRequest request) {
        transportTaskService.updateStatus(orderNo, courierId, request);
        return Result.success();
    }
    @GetMapping("/{orderNo}/tracks")
    public Result<List<TransportTrackVO>> listTracks(@PathVariable String orderNo) {
        return Result.success(transportTaskService.listTracks(orderNo));
    }
}
