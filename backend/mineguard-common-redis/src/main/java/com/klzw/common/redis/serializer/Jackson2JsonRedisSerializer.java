package com.klzw.common.redis.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson2 JSON Redis 序列化器
 */
public class Jackson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = createDefaultObjectMapper();

    @Getter
    private final ObjectMapper objectMapper;
    private final Class<T> clazz;

    public Jackson2JsonRedisSerializer(Class<T> clazz) {
        this(clazz, DEFAULT_OBJECT_MAPPER);
    }

    public Jackson2JsonRedisSerializer(Class<T> clazz, ObjectMapper objectMapper) {
        this.clazz = clazz;
        this.objectMapper = objectMapper;
    }

    /**
     * 创建默认的 ObjectMapper，配置性能优化选项
     * @return ObjectMapper
     */
    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册 Java 8 时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 配置序列化特性
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // 配置日期格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        objectMapper.setDateFormat(dateFormat);
        
        // 启用类型信息，支持多态
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        
        return objectMapper;
    }

    /**
     * 注册自定义序列化器
     * @param type 类型
     * @param serializer 序列化器
     * @param <S> 序列化器类型
     * @return Jackson2JsonRedisSerializer
     */
    @SuppressWarnings("unchecked")
    public <S extends StdSerializer<?>> Jackson2JsonRedisSerializer<T> registerSerializer(Class<?> type, S serializer) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(type, (StdSerializer<Object>) serializer);
        objectMapper.registerModule(module);
        return this;
    }

    /**
     * 注册自定义反序列化器
     * @param type 类型
     * @param deserializer 反序列化器
     * @param <D> 反序列化器类型
     * @return Jackson2JsonRedisSerializer
     */
    @SuppressWarnings("unchecked")
    public <D extends StdDeserializer<?>> Jackson2JsonRedisSerializer<T> registerDeserializer(Class<?> type, D deserializer) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer((Class<Object>) type,deserializer);
        objectMapper.registerModule(module);
        return this;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (Exception e) {
            throw new SerializationException("Could not serialize: " + e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize: " + e.getMessage(), e);
        }
    }

}

