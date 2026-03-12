package com.klzw.common.core.util;

import lombok.Getter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JavaType;
import tools.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class JsonUtils {
    
    @Getter
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            throw new RuntimeException("对象转 JSON 失败", e);
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON 转对象失败", e);
        }
    }
    
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON 转对象失败", e);
        }
    }
    
    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, javaType);
        } catch (JacksonException e) {
            throw new RuntimeException("JSON 转 List 失败", e);
        }
    }
    
    public static <T> List<T> fromJsonToList(String json, Class<T> clazz) {
        return toList(json, clazz);
    }
    
    public static Map<String, Object> toMap(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JacksonException e) {
            throw new RuntimeException("JSON 转 Map 失败", e);
        }
    }
    
    public static boolean isJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        json = json.trim();
        try {
            if (json.startsWith("{") && json.endsWith("}")) {
                objectMapper.readTree(json);
                return true;
            } else if (json.startsWith("[") && json.endsWith("]")) {
                objectMapper.readTree(json);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}