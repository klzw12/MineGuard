package com.klzw.common.web.response;

import com.klzw.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;
import java.util.List;

@Slf4j
@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/v2/api-docs",
            "/swagger-resources",
            "/webjars/",
            "/doc.html",
            "/actuator/"
    );

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();
        
        if (isExcludedPath(path)) {
            return body;
        }

        if (body instanceof Result) {
            return body;
        }

        if (body instanceof String) {
            return body;
        }
        
        return Result.success(body);
    }

    private boolean isExcludedPath(String path) {
        if (path == null) {
            return false;
        }
        
        for (String excludedPath : EXCLUDED_PATHS) {
            if (path.contains(excludedPath)) {
                return true;
            }
        }
        return false;
    }
}
