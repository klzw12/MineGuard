package com.klzw.common.redis.config;

import com.klzw.common.redis.serializer.Jackson2JsonRedisSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedisConfig 测试类
 */
@SpringBootTest
public class RedisConfigTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedisTemplateConfiguration() {
        // 验证RedisTemplate是否配置正确
        assertNotNull(redisTemplate);
        assertNotNull(redisTemplate.getConnectionFactory());
        
        // 验证key序列化器
        assertTrue(redisTemplate.getKeySerializer() instanceof StringRedisSerializer);
        assertTrue(redisTemplate.getHashKeySerializer() instanceof StringRedisSerializer);
        
        // 验证value序列化器
        assertTrue(redisTemplate.getValueSerializer() instanceof Jackson2JsonRedisSerializer);
        assertTrue(redisTemplate.getHashValueSerializer() instanceof Jackson2JsonRedisSerializer);
    }

    @Test
    public void testStringRedisTemplateConfiguration() {
        // 验证StringRedisTemplate是否配置正确
        assertNotNull(stringRedisTemplate);
        assertNotNull(stringRedisTemplate.getConnectionFactory());
    }
}
