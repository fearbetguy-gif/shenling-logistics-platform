package com.sl.express.transport.dto;

import jakarta.validation.constraints.NotBlank;

public record TransportStatusUpdateRequest(
        @NotBlank(message = "目标状态不能为空") String status,
        @NotBlank(message = "轨迹描述不能为空") String description
) {
}
