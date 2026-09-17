package com.sl.express.common.handler;

import com.sl.express.common.exception.BusinessException;
import com.sl.express.common.result.Result;
import com.sl.express.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一转换业务异常、参数校验异常和未知异常，避免 Controller 重复写 try-catch。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException exception) {
        return Result.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public Result<Void> handleValidationException(Exception exception) {
        return Result.failure(ResultCode.BAD_REQUEST.getCode(), "请求参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.failure(ResultCode.SYSTEM_ERROR);
    }
}
