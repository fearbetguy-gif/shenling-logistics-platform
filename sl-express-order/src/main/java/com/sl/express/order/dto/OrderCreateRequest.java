package com.sl.express.order.dto;

import jakarta.validation.constraints.NotBlank;

public record OrderCreateRequest(
        @NotBlank(message = "寄件人姓名不能为空") String senderName,
        @NotBlank(message = "寄件人手机号不能为空") String senderPhone,
        @NotBlank(message = "寄件地址不能为空") String senderAddress,
        @NotBlank(message = "收件人姓名不能为空") String receiverName,
        @NotBlank(message = "收件人手机号不能为空") String receiverPhone,
        @NotBlank(message = "收件地址不能为空") String receiverAddress,
        @NotBlank(message = "物品类型不能为空") String goodsType
) {}
