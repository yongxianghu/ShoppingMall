package com.shop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：把异常统一转成 { code, msg }。
 * 三个分支：业务异常、资源不存在、其它。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 我们自己 throw 的业务异常，比如「库存不足」「用户名已存在」。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 静态资源不存在，最常见的就是浏览器自动请求 /favicon.ico。
     * 这类不算服务器错误，安静地回 404 就行。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResource(NoResourceFoundException e) {
        log.debug("资源不存在: /{}", e.getResourcePath());
        return Result.fail(404, "资源不存在");
    }

    /**
     * 路径参数类型不对，比如 /api/products/abc。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {} = {}", e.getName(), e.getValue());
        return Result.fail(400, "参数 " + e.getName() + " 类型不正确");
    }

    /**
     * 其它没预料到的异常，打日志方便排查。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("服务器异常", e);
        return Result.fail(500, "服务器开小差了，请稍后再试");
    }
}
