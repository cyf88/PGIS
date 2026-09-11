package com.pgis.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> badRequest(IllegalArgumentException ex) {
        log.warn("请求参数错误: {}", ex.getMessage());
        return ApiResponse.fail(400, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<Void> tooLarge(MaxUploadSizeExceededException ex) {
        log.warn("上传文件超过限制: {}", ex.getMessage());
        return ApiResponse.fail(413, "上传文件过大，单文件/整次请求上限为 4GB");
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> multipart(MultipartException ex) {
        log.warn("multipart 解析失败: {}", ex.getMessage());
        return ApiResponse.fail(400, "文件上传失败: " + ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound(NoResourceFoundException ex) {
        log.debug("静态资源不存在: {}", ex.getResourcePath());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> error(Exception ex) {
        log.error("未处理异常", ex);
        String message = ex.getMessage() == null ? "服务器内部错误" : ex.getMessage();
        return ApiResponse.fail(500, message);
    }
}
