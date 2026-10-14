package com.sl.express.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "联系人不能为空") @Size(max = 32) String contactName,
        @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确") String contactPhone,
        @NotBlank(message = "省不能为空") String province,
        @NotBlank(message = "市不能为空") String city,
        @NotBlank(message = "区县不能为空") String district,
        @NotBlank(message = "详细地址不能为空") @Size(max = 255) String detailAddress,
        Integer isDefault
) {
}
