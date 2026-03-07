package com.klzw.common.redis.aspect;

import com.klzw.common.redis.annotation.Cacheable;
import com.klzw.common.redis.annotation.CacheEvict;
import com.klzw.common.redis.service.RedisCacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 缓存切面
 */
@Aspect
@Component
public class CacheAspect {
    
    private final RedisCacheService redisCacheService;
    
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 构造器注入
     * @param redisCacheService Redis缓存服务
     */
    public CacheAspect(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }
    
    /**
     * 处理缓存注解
     * @param joinPoint 连接点
     * @param cacheable 缓存注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(cacheable)")
    public Object handleCacheable(ProceedingJoinPoint joinPoint, Cacheable cacheable) throws Throwable {
        String key = generateKey(cacheable.keyPrefix(), cacheable.keySuffix(), joinPoint);
        
        // 尝试从缓存获取
        Object result = redisCacheService.get(key);
        if (result != null) {
            return result;
        }
        
        // 缓存未命中，执行方法
        result = joinPoint.proceed();
        
        // 存入缓存
        redisCacheService.set(key, result, cacheable.expire(), cacheable.timeUnit());
        
        return result;
    }

    /**
     * 处理缓存清除注解
     * @param joinPoint 连接点
     * @param cacheEvict 缓存清除注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(cacheEvict)")
    public Object handleCacheEvict(ProceedingJoinPoint joinPoint, CacheEvict cacheEvict) throws Throwable {
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 清除缓存
        String key = generateKey(cacheEvict.keyPrefix(), cacheEvict.keySuffix(), joinPoint);
        redisCacheService.delete(key);
        
        return result;
    }

    /**
     * 生成缓存键
     * @param keyPrefix 键前缀
     * @param keySuffix 键后缀
     * @param joinPoint 连接点
     * @return 缓存键
     */
    private String generateKey(String keyPrefix, String keySuffix, ProceedingJoinPoint joinPoint) {
        StringBuilder key = new StringBuilder(keyPrefix);
        
        if (!keySuffix.isEmpty()) {
            // 解析SpEL表达式
            String resolvedSuffix = resolveSpEL(keySuffix, joinPoint);
            key.append(":").append(resolvedSuffix);
        }
        
        return key.toString();
    }

    /**
     * 解析SpEL表达式
     * @param expression SpEL表达式
     * @param joinPoint 连接点
     * @return 解析结果
     */
    private String resolveSpEL(String expression, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        
        // 创建表达式上下文
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                joinPoint.getTarget(), method, joinPoint.getArgs(), parameterNameDiscoverer);
        
        // 解析表达式
        return parser.parseExpression(expression).getValue(context, String.class);
    }
}
