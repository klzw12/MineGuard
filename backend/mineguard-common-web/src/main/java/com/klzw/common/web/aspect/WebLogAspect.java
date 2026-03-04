package com.klzw.common.web.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Web 日志切面
 * 合并了 ControllerLogAspect 和 RequestLogAspect 的功能
 * 同时提供请求日志记录和性能监控
 */
@Slf4j
@Aspect
@Component
public class WebLogAspect {
    
    // 统一切入点：拦截所有控制器方法
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void webLogPointcut() {
    }
    
    // 环绕通知，合并所有功能
    @Around("webLogPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
            // 记录请求信息（info级别）
            log.info("Request: {}", request.getRequestURL().toString());
            log.info("Method: {}", request.getMethod());
            log.info("IP: {}", request.getRemoteAddr());
        }
        
        // 记录方法信息
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.info("Class: {}, Method: {}", className, methodName);
        log.info("Parameters: {}", Arrays.toString(joinPoint.getArgs()));
        
        long startTime = System.currentTimeMillis();
        Object result = null;
        
        try {
            // 执行方法
            result = joinPoint.proceed();
            
            // 计算执行时间
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            
            // 性能监控（debug级别，带阈值）
            if (log.isDebugEnabled() && costTime > 100) {
                log.debug("Controller Method: {}#{}, Execution Time: {}ms", 
                        className, methodName, costTime);
            }
            
            // 记录响应信息（info级别）
            log.info("Response: {}", result);
            
            return result;
        } catch (Throwable e) {
            // 计算执行时间
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            
            // 记录异常信息
            log.error("Controller Method: {}#{}, Execution Time: {}ms, Exception: {}", 
                    className, methodName, costTime, e.getMessage(), e);
            
            throw e;
        }
    }
    
}
