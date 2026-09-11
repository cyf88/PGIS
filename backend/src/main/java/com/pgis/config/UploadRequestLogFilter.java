package com.pgis.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 在 multipart 解析之前打日志。大文件会先完整接收再进 Controller，期间若无日志会像“卡住”。
 */
@Component
public class UploadRequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(UploadRequestLogFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith("/api/import");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long contentLength = request.getContentLengthLong();
        log.info("开始接收导入请求 {} contentLength={}", request.getRequestURI(), contentLength);
        long started = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("导入请求结束 {} status={} elapsedMs={}",
                    request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - started);
        }
    }
}
