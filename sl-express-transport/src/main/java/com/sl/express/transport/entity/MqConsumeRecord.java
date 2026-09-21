package com.sl.express.transport.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("mq_consume_record")
public class MqConsumeRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageKey;
    private String messageType;
    private LocalDateTime consumedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMessageKey() { return messageKey; }
    public void setMessageKey(String messageKey) { this.messageKey = messageKey; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public LocalDateTime getConsumedAt() { return consumedAt; }
    public void setConsumedAt(LocalDateTime consumedAt) { this.consumedAt = consumedAt; }
}
