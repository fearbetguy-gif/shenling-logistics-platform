package com.sl.express.transport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

@TableName("route_edge")
public class RouteEdgeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fromNodeCode;
    private String toNodeCode;
    private BigDecimal distanceKm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFromNodeCode() { return fromNodeCode; }
    public void setFromNodeCode(String fromNodeCode) { this.fromNodeCode = fromNodeCode; }
    public String getToNodeCode() { return toNodeCode; }
    public void setToNodeCode(String toNodeCode) { this.toNodeCode = toNodeCode; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }
}
