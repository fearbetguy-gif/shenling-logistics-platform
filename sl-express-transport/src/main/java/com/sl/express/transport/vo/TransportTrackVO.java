package com.sl.express.transport.vo;

import java.time.LocalDateTime;

public record TransportTrackVO(String status, String description, LocalDateTime createdAt) {
}
