package com.klzw.common.redis.aspect;

import com.klzw.common.redis.annotation.RateLimit;
import com.klzw.common.redis.constant.RedisResultCode;
import com.klzw.common.redis.exception.RedisException;
import com.klzw.common.redis.service.RedisRateLimiter;
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
 * 限流切面
 */
@Aspect
@Component
public class RateLimitAspect {

    private final RedisRateLimiter redisRateLimiter;

    public RateLimitAspect(RedisRateLimiter redisRateLimiter) {
        this.redisRateLimiter = redisRateLimiter;
    }

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * 处理限流注解
     *
     * @param joinPoint 连接点
     * @param rateLimit 限流注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(rateLimit)")
    public Object handleRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = generateKey(rateLimit.keyPrefix(), rateLimit.keySuffix(), joinPoint);

        // 检查限流
        boolean allowed = redisRateLimiter.tryAcquire(
                key,
                rateLimit.limit(),
                rateLimit.window(),
                rateLimit.timeUnit()
        );

        if (!allowed) {
            throw new RedisException(RedisResultCode.RATE_LIMIT_EXCEEDED.getCode(), rateLimit.message());
        }

        // 执行方法
        return joinPoint.proceed();
    }

    /**
     * 生成限流键
     *
     * @param keyPrefix 键前缀
     * @param keySuffix 键后缀
     * @param joinPoint 连接点
     * @return 限流键
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
     *
     * @param expression SpEL表达式
     * @param joinPoint  连接点
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
