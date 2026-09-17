package com.sl.express.common.exception;

import com.sl.express.common.result.ResultCode;

/**
 * 可预期的业务异常，例如订单状态不允许取消。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
