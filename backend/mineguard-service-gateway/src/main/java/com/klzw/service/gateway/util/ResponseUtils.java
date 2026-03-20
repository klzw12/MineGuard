package com.klzw.service.gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 响应工具类
 */
@Slf4j
public class ResponseUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ResponseUtils() {
    }

    public static Mono<Void> writeError(ServerHttpResponse response, HttpStatus status, int code, String message) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        Result<Void> result = Result.fail(code, message);
        
        try {
            byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("JSON序列化失败", e);
            String errorJson = "{\"code\":500,\"message\":\"Internal Server Error\",\"data\":null}";
            DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    public static Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        return writeError(response, HttpStatus.UNAUTHORIZED, 401, message);
    }

    public static Mono<Void> forbidden(ServerHttpResponse response, String message) {
        return writeError(response, HttpStatus.FORBIDDEN, 403, message);
    }

    public static Mono<Void> serverError(ServerHttpResponse response, String message) {
        return writeError(response, HttpStatus.INTERNAL_SERVER_ERROR, 500, message);
    }

    public static Mono<Void> serviceUnavailable(ServerHttpResponse response, String message) {
        return writeError(response, HttpStatus.SERVICE_UNAVAILABLE, 503, message);
    }
}
