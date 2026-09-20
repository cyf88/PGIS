package com.pgis.common;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> badRequest(IllegalArgumentException ex, HttpServletResponse response) {
        log.warn("请求参数错误: {}", ex.getMessage());
        forceJson(response);
        return ApiResponse.fail(400, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<Void> tooLarge(MaxUploadSizeExceededException ex, HttpServletResponse response) {
        log.warn("上传文件超过限制: {}", ex.getMessage());
        forceJson(response);
        return ApiResponse.fail(413, "上传文件过大，单文件/整次请求上限为 4GB");
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> multipart(MultipartException ex, HttpServletResponse response) {
        log.warn("multipart 解析失败: {}", ex.getMessage());
        forceJson(response);
        return ApiResponse.fail(400, "文件上传失败: " + ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound(NoResourceFoundException ex) {
        log.debug("静态资源不存在: {}", ex.getResourcePath());
    }

    /**
     * 浏览器切换/关闭页面时会中断 .js/.css 等静态资源的下载，连接已断开，
     * 既不需要也无法再写回响应体，仅记录 debug 日志。
     */
    @ExceptionHandler({ClientAbortException.class, AsyncRequestNotUsableException.class})
    public void clientAbort(Exception ex) {
        log.debug("客户端中断连接: {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> error(Exception ex, HttpServletResponse response) {
        // 响应已提交（部分内容已写出）或客户端已断开时，无法再写回 JSON
        if (response.isCommitted() || isClientAbort(ex)) {
            log.debug("响应已提交或客户端已断开，无法写回错误信息: {}", ex.getMessage());
            return null;
        }
        log.error("未处理异常", ex);
        // 静态资源（如 .js）处理过程中抛异常时，Content-Type 已被预设为 text/javascript，
        // 不重置成 JSON 会导致 ApiResponse 找不到可用的消息转换器而再次报错
        forceJson(response);
        String message = ex.getMessage() == null ? "服务器内部错误" : ex.getMessage();
        return ApiResponse.fail(500, message);
    }

    private static void forceJson(HttpServletResponse response) {
        if (!response.isCommitted()) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        }
    }

    private static boolean isClientAbort(Throwable ex) {
        Throwable current = ex;
        int depth = 0;
        while (current != null && depth++ < 20) {
            if (current instanceof ClientAbortException
                    || current instanceof AsyncRequestNotUsableException
                    || current.getClass().getName().endsWith(".EofException")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
